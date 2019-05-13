import graphene
from graphene_django import DjangoObjectType
from django.db.models import Q   #for advanced OR Querying, not needed
from graphql import GraphQLError

#from users.schema import UserType  #Will implement soon
from .models import Chore

from users.models import User
from households.models import Household
from resources.models import Resource



class ChoreType(DjangoObjectType):
    class Meta:
        model = Chore

class ResourceCreateInput(graphene.InputObjectType):
    name = graphene.String(required = True)
    checked = graphene.Boolean(required = True)




#Unused
'''
class GetChores(graphene.ObjectType):
    chores = graphene.List(ChoreType, user_id = graphene.Int(), status = graphene.Int(), count = graphene.Int(), offset = graphene.Int())

    def resolve_chores(self, info, user_id = None, status = None, count = None, offset = None, **kwargs):
       qs = Chore.objects.all()
       if user_id:
            filter = Q(assigned_to__id__exact=user_id)  #This actually works lol
            qs = qs.filter(filter)

       if status:
           filter = Q(status__exact = status)
           qs = qs.filter(filter)
           if status == 0:   #Status completed
               qs.order_by('-deadline')   #ordering by most recently completed and then down
           else:
               qs.order_by('deadline')
       if offset:
           qs = qs[offset::]
       if count:
           qs = qs[:count]


       return qs



class FindChore(graphene.ObjectType):
    chore = graphene.List(ChoreType, chore_id = graphene.Int())


    def resolve_chore(self, info, chore_id, **kwargs):
        filter = Q(assigned_to__id__exact = chore_id)
        return Chore.objects.get(filter)
'''




class Query(graphene.ObjectType):
    chores = graphene.List(ChoreType, user_id=graphene.Int(), status=graphene.Int(), count=graphene.Int(), offset=graphene.Int())
    chore = graphene.List(ChoreType, chore_id=graphene.Int())

    def resolve_chores(self, info, user_id = None, status = None, count = None, offset = None, **kwargs):
       qs = Chore.objects.all()
       if user_id:
            filter = Q(assigned_to__id__exact=user_id)  #This actually works lol
            qs = qs.filter(filter)

       if status:
           filter = Q(status__exact = status)
           qs = qs.filter(filter)
           if status == 0:   #Status completed
               qs.order_by('-deadline')   #ordering by most recently completed and then down
           else:
               qs.order_by('deadline')
       if offset:
           qs = qs[offset::]
       if count:
           qs = qs[:count]


       return qs

    def resolve_chore(self, info, chore_id, **kwargs):
        filter = Q(assigned_to__id__exact = chore_id)
        return Chore.objects.get(filter)









#Now for Mutations


#AddChore has the added benefit of having to deal with the Resource object

class AddChore(graphene.Mutation):
    chore = graphene.Field(ChoreType)

    class Arguments:
        name = graphene.String(),
        description= graphene.String(),
        isAllDay= graphene.Boolean,
        points= graphene.Int(),
        status= graphene.Int(),
        recurrence_pattern= graphene.Int(),
        recurrence_enddate= graphene.DateTime(),
        deadline= graphene.DateTime(),
        creator= graphene.Int(),  # userid
        assignee= graphene.ID(),  # userid
        resources= graphene.List(ResourceCreateInput)



    def mutate(self, info, isAllDay, points, status, recurrence_pattern, recurrence_enddate, deadline, creator, assignee = None, resources = None, name = None, description = None):


        cUser = User.objects.filter(id=creator).first()
        if not cUser:
            raise GraphQLError('Invalid Link!')
        if assignee != None:
            aUser = User.objects.filter(id=assignee).first()
            if not aUser:
                raise GraphQLError('Invalid Link!')
        else:
            aUser = None
        household = Household.objects.filter(id = 0)
        chore = Chore(
            name = name,
            description = description,
            isAllDay = isAllDay,
            points = points,
            status = status,
            recurrence_pattern = recurrence_pattern,
            recurrence_enddate = recurrence_enddate,
            deadline = deadline,
            created_by = cUser,
            assigned_to = aUser,

        )
        chore.save()
        # I'M REALLY NOT SURE THIS NEXT PART WILL WORK

        if resources != None:
            for x in resources: #each x is of type ResourceCreateInput
                resource = Resource(
                    name = x.name,
                    checked = x.checked,
                    assigned_to = Chore.objects.get(id__exact = chore.id)
                )
                resource.save()


        return AddChore(
            id = chore.id,
        )


class UpdateChore(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        chore_id = graphene.Int(),
        name = graphene.String(),
        description = graphene.String(),
        isAllDay = graphene.Boolean,
        points = graphene.Int(),
        status = graphene.Int(),
        deadline = graphene.DateTime(),
        assignee = graphene.Int(),
        resources = graphene.List(ResourceCreateInput)

    def mutate(self, info, chore_id, isAllDay, points, status, deadline, assignee = None, name = None, description = None, resources = None ):
        chore = Chore.objects.get(id__exact = chore_id)
        if not chore:
            raise GraphQLError('Invalid Chore!')

        setattr(chore, 'isAllDay', isAllDay)

        setattr(chore, 'points', points)

        setattr(chore, 'status', status)
        setattr(chore, 'deadline', deadline)
        if assignee:
            setattr(chore, 'assignee', assignee)
        if name:
            setattr(chore, 'name', name)
        if description:
            setattr(chore, 'description', description)
        if resources:
            old_resources = Resource.objects.filter(assigned_to = chore)

            #deleting all old resources (since resource ID's are not utilized by FE update function for chore
            for y in old_resources:
                y.delete()
            #All old resources should be deleted by now.

            #we CAN run a check here for that if we need to, do that if it starts acting up

            #Now lets assign the resources just as we did in the create mutation
            for x in resources:
                resource = Resource(
                    name=x.name,
                    checked=x.checked,
                    assigned_to=Chore.objects.get(id__exact=chore_id)
                )
                resource.save()

        chore.save()

        return UpdateChore(success = True)


class DeleteChore(graphene.Mutation):
    success = graphene.Boolean

    class Arguments:
        chore_id = graphene.Int()


    def mutate(self, info, chore_id):
        chore = Chore.objects.get(id__exact=chore_id)


        #Now I'm pretty sure that resources will be deleted within the cascade delete when a chore gets deleted that they're attached to, but for now, we're going to delete the
        #attached resources first, and then delete the chore.

        old_resources = Resource.objects.filter(assigned_to=chore)
        for y in old_resources:
            y.delete()

        chore.delete()

        return DeleteChore(success = True)


class DeleteAllChores(graphene.Mutation):
    success = graphene.Boolean


    def mutate(self, info):
        all_chores = Chore.objects.all()

        for c in all_chores:
            chore = c

            old_resources = Resource.objects.filter(assigned_to=chore)
            for y in old_resources:
                y.delete()

            chore.delete()

        return DeleteAllChores(success = True)



class Mutation(graphene.ObjectType):
    add_chore = AddChore.Field()
    update_chore = UpdateChore.Field()
    delete_chore = DeleteChore.Field()
    delete_all_chores = DeleteAllChores.Field()



#Chores Schema Complete

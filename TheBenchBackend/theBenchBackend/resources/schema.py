import graphene
from graphene_django import DjangoObjectType

from .models import Resource

from chores.models import Chore


from graphql import GraphQLError
from django.db.models import Q

class ResourceType(DjangoObjectType):
    class Meta:
        model = Resource

class ResourceCreateInput(graphene.InputObjectType):
    name = graphene.String(required = True)
    checked = graphene.Boolean(required = True)
    chore_id = graphene.Int(required = True)



#Queries

'''
class GetResources(graphene.ObjectType):
    resources = graphene.List(ResourceType, chore_id = graphene.Int())

    def resolve_resources(self, info, chore_id):
        chore = Chore.objects.get(id = chore_id)
        qs = Resource.objects.filter(assigned_to = chore)

        return qs
'''

class Query(graphene.ObjectType):
    resources = graphene.List(ResourceType, chore_id=graphene.Int())

    def resolve_resources(self, info, chore_id):
        chore = Chore.objects.get(id=chore_id)
        qs = Resource.objects.filter(assigned_to=chore)

        return qs



#Mutations



class AddResource(graphene.Mutation):
    assigned_id = graphene.Int() #id of chore it was assigned to(already included in input field)

    class Arguments:
        resource = ResourceCreateInput(required=True)

    def mutate(self, info, resource):
         chore = Chore.objects.get(id = ResourceCreateInput.chore_id)
         if not chore:
             raise GraphQLError('Invalid Chore ID!')

         r = Resource(
             name = ResourceCreateInput.name,
             checked = ResourceCreateInput.checked,
             assigned_to = chore
         )
         r.save()


         return AddResource(assigned_id = ResourceCreateInput.chore_id)

class UpdateResource(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        resource = ResourceCreateInput(required=True)

    def mutate(self, info, resource):
        chore = Chore.objects.get(id = ResourceCreateInput.chore_id)
        if not chore:
            raise GraphQLError('Invalid Chore ID!')

        #this is where it gets tricky
        #We're going to filter by the name and the chore it is assigned to, then verify against the checked status.from
        #We'll apply the logic that there can be more than one resource of the same name, assigned to the same chore. And we'll also understand that it's possible that we'll be updating a chore to the same state as it was before.from
        #We'll preferably look to change the first "matched duplicate" that has a checked state different to that of the checked state in the passed resource

        #This would be much more simple with the basic ability to directly search by resource id

        filter = (Q(name__iexact = ResourceCreateInput.name) & Q(assigned_to = chore))

        qs = Resource.objects.filter(filter)

        for x in qs:
            if (x.checked != ResourceCreateInput.checked):
                x.checked = ResourceCreateInput
                x.save()
                break

        #will always return true since update doesn't fail if no actual update happens, only fails if save fails
        return UpdateResource(success = True)

class DeleteResource(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        resource = ResourceCreateInput(required=True)

    def mutate(self, info, resource):
        chore = Chore.objects.get(id=ResourceCreateInput.chore_id)
        if not chore:
            raise GraphQLError('Invalid Chore ID!')

        filter = (Q(name__iexact=ResourceCreateInput.name) & Q(checked= ResourceCreateInput.checked) & Q(assigned_to=chore))

        qs = Resource.objects.filter(filter).first()

        qs.delete() #deletes found resource


class Mutation(graphene.ObjectType):
    add_resource = AddResource.Field()
    update_resource = UpdateResource.Field()
    delete_resource = DeleteResource.Field()


#Resource Schema Complete

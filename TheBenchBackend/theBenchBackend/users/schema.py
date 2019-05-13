import graphene
from graphene_django import DjangoObjectType
from django.db.models import Q   #for advanced OR Querying, not needed
from graphql import GraphQLError

from households.models import Household


from .models import User

class UserType(DjangoObjectType):
    class Meta:
        model = User


class Query(graphene.ObjectType):
    users = graphene.List(UserType, user_id = graphene.Int(), name = graphene.String())
    #users = graphene.List(UserType)

    def resolve_user(self, info, user_id = None, name = None, **kwargs):
        qs = User.objects.all()

        if user_id:
            filter = Q(id__exact = user_id)
            qs = qs.filter(filter).first()  #returns only the first object, hence not a list returned. For FindUser(userID)

        if name:
            filter = Q(name__iexact = name)
            qs = qs.filter(filter).first() #returns only the first exact matching user (just like previous DB did)

        return qs #returns qs, a list if for all users, a single user if searched by the paramaters available




# Mutations



class AddUser(graphene.Mutation):
    user_id = graphene.ID()

    class Arguments:
        name = graphene.String()
        is_admin = graphene.Boolean()
        password = graphene.String()
        points = graphene.Int()
        avatar = graphene.String() #Actually going to be a byte array converted to a string and sent over

    def mutate(self, info, name, is_admin, points, password = None, avatar = None):
        household = Household.objects.all()[0]

        if is_admin == True:
            if password == None:
                raise GraphQLError("Is Admin, but no password was provided")
            if avatar:
                user = User(
                    name =name,
                    is_admin = is_admin,
                    points = points,
                    password = password,
                    avatar = avatar,
                    belongs_to = household
                )
            else:
                user = User(
                    name=name,
                    is_admin=is_admin,
                    points=points,
                    password=password,
                    belongs_to=household
                )
        else:
            if avatar:
                user = User(
                    name=name,
                    is_admin=is_admin,
                    points=points,
                    avatar=avatar,
                    belongs_to=household
                )
            else:
                user = User(
                    name=name,
                    is_admin=is_admin,
                    points=points,
                    belongs_to=household
                )
        user.save()

        #just in case the user.id doesn't operate correctly
        '''
        filter = (Q(name__exact = name) & Q(points__exact = points) & Q(is_admin = is_admin))
        u = User.objects.get(filter)
        user_id = u.id
        '''
        return AddUser(user_id = user.id)


class UpdateUser(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        user_id = graphene.Int()
        name = graphene.String()
        is_admin = graphene.Boolean()
        password = graphene.String()
        points = graphene.Int()
        avatar = graphene.String()

    def mutate(self, info, user_id, name, is_admin, points, password = None, avatar = None):
        user = User.objects.get(id = user_id)

        user.name = name
        user.is_admin = is_admin
        user.points = points
        if is_admin == True:
            if password == None:
                raise GraphQLError("Is Admin, but no password was provided")
            user.password = password
        if avatar != None:
            user.avatar = avatar

        user.save()

        return UpdateUser(success = True)

class DeleteUser(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        user_id = graphene.ID()

    def mutate(self, info, user_id):

        user = User.objects.get(id=user_id)

        user.delete()

        #may have to end up going and deleting all owned chores to keep DB happy, not sure yet.
        #DB handler doesn't seem to have to though.

        return DeleteUser(success = True)

class Mutation(graphene.ObjectType):
    add_user = AddUser.Field()
    update_user = UpdateUser.Field()
    delete_user = DeleteUser.Field()


#END OF USER SCHEMA


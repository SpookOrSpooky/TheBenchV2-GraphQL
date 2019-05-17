import graphene
from graphene_django import DjangoObjectType
from graphql import GraphQLError

from .models import Household

class HouseholdType(DjangoObjectType):
    class Meta:
        model = Household


class Query(graphene.ObjectType):
    household = graphene.List(HouseholdType)

    def resolve_household(self, info, **kwargs):
        qs = Household.objects.all()

        if qs[0] != None:
            raise GraphQLError('Too Many Households! How is this possible?')

        return qs[0]




# Mutations

class AddHousehold(graphene.Mutation):
    household = graphene.Field(HouseholdType)

    class Arguments:
        name = graphene.String(required = True)

    def mutate(self, info, name):

        qs = Household.objects.all()

        if qs[0] != None:
            raise GraphQLError("Trying to create a Household when there already exists one!")

        household = Household(
            name = name
        )
        household.save()


        return None

class UpdateHousehold(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        name = graphene.String(required= True)

    def mutate(self, info, name):

        qs = Household.objects.all()

        if qs[0] == None:
            raise GraphQLError("Trying to update Household when none exists!")

        household = qs[0]
        household.name = name
        household.save()

        return UpdateHousehold(success = True)


class Mutation(graphene.ObjectType):
    add_household = AddHousehold.Field()
    update_household = UpdateHousehold.Field()



#End Of Household Schema
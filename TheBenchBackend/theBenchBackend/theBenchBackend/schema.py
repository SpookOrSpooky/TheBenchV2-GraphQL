import graphene

import chores.schema
import households.schema
import resources.schema
import users.schema


class Query(
    chores.schema.Query,
    households.schema.Query,
    resources.schema.Query,
    users.schema.Query,
    graphene.ObjectType
):
    pass


class Mutation(
    chores.schema.Mutation,
    households.schema.Mutation,
    resources.schema.Mutation,
    users.schema.Mutation,
    graphene.ObjectType
):
    pass

schema = graphene.Schema(query = Query, mutation = Mutation)


#Primary Schema Complete
from django.db import models

class User(models.Model):
    name = models.TextField(blank=False)
    password = models.TextField(blank=True)

    is_admin = models.BooleanField(blank=False) #pay attention to this blank field for the future
    points = models.IntegerField(blank=False)

    #The following are the associations that the User model has to other models.

    #Household association, one to one, belongs to(reverse key lookup)
    belongs_to = models.ForeignKey(
        'households.Household',
        related_name="users",
        on_delete=models.SET_NULL,
        null=True,
    )

    #Created_chores association list.
        #no need, association done already at Chore. query by user -> choresCreated

    #assigned chores association list
    # no need, association done already at Chore. query by user -> choresAssigned

    # user avatar can be stored as a BLOB (binary data)

    #avatar = models.BinaryField(bytes)

    #we are going to use a string to capture the avatar instead, and do string to byte array conversions to keep things clean on the DB end

    avatar = models.TextField(blank= True)
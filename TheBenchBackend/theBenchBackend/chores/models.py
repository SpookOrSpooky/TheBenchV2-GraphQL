from django.db import models


#Implemented! Not verified.

class Chore(models.Model):
    #only items required are the
    name = models.TextField(blank=True)              #
    description = models.TextField(blank=True)
    isAllDay = models.BooleanField(blank=False)
    points = models.IntegerField(blank=False)

    #The following is the "Chore Status" enum from the Chore.Status category.
    #Due to time constraints, I'm going to make this an int that will evaluate client side
    #Here, 0= "COMPLETED", 1= "ACTIVE" and 2 = "POSTPONED.
    status = models.IntegerField(blank=False, null=False)

    #The same happens for Recurrence Pattern, another enum
    #Here, 0 = "NONE", 1 = "DAILY", 2 = "WEEKLY", and 3 = "MONTHLY"
    recurrence_pattern = models.IntegerField(null=False, blank=False)

    #RecurrenceEndDate is of type Date, but we're ok with it being null, it's what we expect since the feature is not
    # yet implemented.
    recurrence_enddate = models.DateTimeField(null=True, blank=False)

    #deadline is a datetime object that IS required
    deadline = models.DateTimeField(blank=False)


    #The following are the associations for chore to other objects

    #creator association( to a single User). when pulling value, look for the first (and only) value
    created_by = models.ForeignKey(
        'users.User',
        related_name='chores_created',             #the users -> choresCreated query allows me to show all the chores a user has made
        on_delete=models.SET_NULL,
        null=True,
    )

    #assignee association (to a single User)
    #MUST TEST IF YOU CAN QUERY users -> chores
    assigned_to = models.ForeignKey(
        'users.User',
        related_name='chores_assigned',            #the users -> choresAssigned query allows me to show all the chores a user has been assigned
        on_delete=models.SET_NULL,
        null=True,
    )



    #resources[]  association (a list of Resources

        #We actually aren't going to make a resource association. By creating an association

    belongs_to = models.ForeignKey(
        'households.Household',
        related_name="chores",
        on_delete=models.SET_NULL,
        null=True,
    )

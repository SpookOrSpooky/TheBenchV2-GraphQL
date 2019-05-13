from django.db import models

class Household(models.Model):
    name = models.TextField(blank=False, null=False)


    #Associations

    #User list association ( may just implement a reverse relationship, just as in votes to links


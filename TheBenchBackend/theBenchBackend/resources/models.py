from django.db import models

class Resource(models.Model):
    name = models.TextField(blank=False, null=False)

    checked = models.BooleanField(blank=False, null=False)


    # "choreID" association. Associating the whole chore instead, able to then query to the chore ID as needed.
    assigned_to = models.ForeignKey(
        'chores.Chore',
        related_name='resources',
        on_delete=models.CASCADE
    )

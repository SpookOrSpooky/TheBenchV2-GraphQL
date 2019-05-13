## The Bench v2, GraphQL Boogaloo

This is a solo undertaking, consisting of a complete backend overhaul from an existing DB system built for the entirely new call/query format of GraphQL. The purpose of such an undertaking is rather less what the end-result can accomplish, and more so a demonstration of skill in the area of rapid software infrastructure restructuring within unfamiliar development environments, acting as an example for both general software architectural prowess and adaptation to limitations of design. My very first exposure to any type of backend with a GraphQL API structure was on May 8th, 2019. By May 13th, I had fully designed a complete backend environment, adapted to refit the localized DB structure used on the original TheBench android application.

I will be continuing development, but due to the limitations of the Apollo-Android alpha, it is possible that full integration of back and front end will not be possible until further issue patches are released for the library. There is no other alternative at this moment in time.


The current state of the project:

 --------------------------------------------**BACKEND**(*Graphene-Django*) - **Core Complete**-----------------------------------------

All models and schema's are completed. Compile and DB migration commands are successful. Schema *not ready* for translation to front-end. 

#### Issues:
        ValueError's out the GET request upon reaching the AddChore Arguments. Definitive failure on graphene-django end, full compile 
        and DB migration successful. Possible open Django-graphene bug, too soon to know. Issue ticket submitted. 
        

 --------------------------------------------**FRONTEND**(*Apollo-Android*) - **App Complete**-----------------------------------------
 
Android Application completed. Apollo-Android packages installed and implemented into the app Gradle build scripts. Clean library installs for all relevant functions. Minor deprecation re-vamp work done to change outdated function calls. Awaiting schema translation with Apollo-CLI. A test schema from the /graphql-python-intro/ project has been placed withing the project for testing purposes. 

### Issues:
        None so far.



**Languages Used**: 
- Python 3.6
- Java JDK 11.0.2
- JSON 2.2.0

**Query Languages Used**: 
- GraphQL
- SQLite3

**Other Technologies Used**: 
- Django 2.2
- Graphene-Django 2.2.0
- Django-Filter 2.1.0
- Apollo-Android 1.0.0-alpha5
- Gradle 3.3.2

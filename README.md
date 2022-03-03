# University-Registration-Companion.
*University Registration Companion* is an all-around mechanism designed to assist the University Account creation procedure.

During the procedure of creating a new University Account, a loginName for the new account must be decided. Many university services use this loginName as a personal identifier and therefore it must be unique per person across multiple registries.

These registries are:
* The Student Information System (SIS).
* The Human Resource Management Software (HRMS).
* A Second Human Resource Management Software concerning only the
Associates (HRMS2/Associates).
* The Data Server (DS).

The *University Registration Companion* assists the user in picking a suitable loginName during the account creation process by offering the following services/endpoints:
* ***loginNameValidator/*** - checks if the provided loginName can be assigned to the specified person.
* ***loginNameSuggester/*** - recommends a valid loginName for the specified person.
* ***roleFinder/*** - provides information regarding the specified person's roles in the University (Student, Member of the Teaching Staff, Associate etc), if they already have such a role.

The endpoints expect to receive a **JSON** formatted request and respond likewise.

## Quick Start
### loginNameValidator
During the procedure of creating a new University Account, one must examine whether a proposed loginName for that Account would generate any *conflicts* with the already present data across all 4 databases.  

A *conflict* occurs when the requested *loginName* is already attributed to a different university entity than the one requesting that specific *loginName*.   

The endpoint **loginNameValidator** is a mechanism that will locate any conflicts or inconsistencies if this *loginName* were to be used.  

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/loginNameValidator).

### loginNameSuggester
Selecting a loginName for a new University Account is often not a straightforward procedure.
Some universities use standardized patterns for creating new *loginNames*, a strategy that pretty much ensures their uniqueness, while others expect from the applicants to come up with their own *unique loginName*.  

The endpoint **loginNameSuggester** addresses the latter scenario, suggesting possible loginNames for a new Account, by:
* Locating previous loginNames based on given *SSN* & *SSNCountry*.
* Generating new loginNames based on given *firstName* & *lastName* in the request.  

The endpoint shall propose loginNames that can be safely attributed to a new Account.  

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/loginNameSuggester).

### roleFinder
A difficult task for a university with thousands of active members can be to determine a person's Role in their institution. 
Search for a person's active Roles in a institution based on a given userName.

The endpoint **roleFinder** conducts a search based on a given loginName and decides whether a person is a *Student*, a *Member of the Teaching Staff*, an *Associate*, or a combination of the above.

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder).

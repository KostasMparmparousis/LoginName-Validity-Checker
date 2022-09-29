# uLookup.
The lifecycle of a university member usually starts with the registration in one of the following information systems, depending on the applicant's role: 
* The **Student Information System** (***SIS***), for students.
* The **Human Resource Management Software** (***HRMS***)  of the University, for faculty members, staff etc.
* The **Human Resource Management Software of the Research Committee** (***ELKE***), for contractual staff, associates, researchers etc.
* The **Directory Service** (***DS/LDAP***) for any other type of members that do not fit into any of the above categories such as the guests accounts.

This distributed schema of entry points, has been dictated mostly by the universities' organizational structure and the fact that the authoritative systems where you create, manage and delete identities, have been organized vertically i.e. one authoritative system for students, one for staff members, other of researchers etc. This design choice comes with its pros and cons. 

<img src="https://i.ibb.co/jvfJCVF/test-1.png" alt="test-1" style="width: 66%;" border="0">

 It is certainly more agile in many aspects considering that each system serves different types of requirements, however in terms of Identity Management introduces the problem of profiles reconciliation and complicates the personal identifiers management process, since there is no central registration office.

The most common personal identifier in the digital world is the principal name of the account, which is required to access the university's digital services i.e. the username. The username should be: 
* **unique** per person/account across all university registries.
* **human-palatable** in order to be rememberable and reproducible by typical human users.
* **persistent** for the lifetime of the account or even more considering that the username:
    * could be allocated at the registration phase, even before the account creation in the directory service (LDAP).
    * may need to be reserved for a specific time period following the deprovisioning of the account from the directory service.

**uLookup** is an all-around mechanism designed to **assist in the allocation of a username** to a new university member or to an additional account of an existing member. The service checks for the availability of a username across all authoritative identity sources and can notify for usernames that have already been paired with the same person on a another authoritative source, thus should be used instead. In addition, via the uLookup service the university can define a global policy regarding the format and the algorithm that should be used to construct a username.

The intended users of the uLookup service are:
* the application stacks of the four authoritative identity sources, which may access the service **via its API**.
* the administrators of the institutional users' catalog (DS/LDAP) **via the uLookup web application**.

*uLookup* offers the following services/endpoints:
* **Validator** - checks if the provided loginName can be assigned to the specified person.
* **Proposer** - recommends a valid loginName for the specified person.
* **Finder** - provides information regarding the specified person's roles in the University (Student, Member of the Teaching Staff, Associate etc), if they already have such a role.

The endpoints respond with a **JSON** formatted answer. 
## Quick Start
### Validator
During the procedure of creating a new University Account, one must examine whether a proposed loginName for that Account would generate any *conflicts* with the already present data across all 4 databases.  

A *conflict* occurs when the requested *loginName* is already attributed to a different university entity than the one requesting that specific *loginName*.   

The endpoint **Validator** is a mechanism that will locate any conflicts or inconsistencies if this *loginName* were to be used.  

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator).

### Proposer
Selecting a loginName for a new University Account is often not a straightforward procedure.
Some universities use standardized patterns for creating new *loginNames*, a strategy that pretty much ensures their uniqueness, while others expect from the applicants to come up with their own *unique loginName*.  

The endpoint **Proposer** addresses the latter scenario, suggesting possible loginNames for a new Account, by:
* Locating previous loginNames based on given *SSN* & *SSNCountry*.
* Generating new loginNames based on given *firstName* & *lastName* in the request.  

The endpoint shall propose loginNames that can be safely attributed to a new Account.  

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Proposer).

### Finder
A difficult task for a university with thousands of active members can be to determine a person's Role in their institution. 
Search for a person's active Roles in a institution based on a given userName.

The endpoint **Finder** conducts a search based on a given loginName and decides whether a person is a *Student*, a *Member of the Teaching Staff*, an *Associate*, or a combination of the above.

A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Finder).

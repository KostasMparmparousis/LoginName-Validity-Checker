# LoginName-Validity-Checker.
LoginName Validity Checker is an all-around mechanism that is utilized during the University Account creation procedure.

## Endpoints
* ***loginNameValidator/***    
* ***loginNameSuggestor/***    
* ***roleFinder/*** 

The endpoints expect to receive a **JSON** formatted request and respond likewise.

## Quick Start
### loginNameValidator
Examines if a given loginName can be safely attributed to a new Account.  
As part of a request it expects to receive a person's:
* *SSN*.
* *SSNCountry*.
* *TIN* (if available).
* *TINCountry* (if available).
* *birthDate*.
* *birthYear*.
* *loginName*.
* *institution*.

The endpoint will locate any conflicts or inconsistencies if this *loginName* were to be used.  
A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/loginNameValidator).

### loginNameSuggestor
Responsible for suggesting possible loginNames for a new Account, by:
* Locating previous userNames based on *SSN* & *SSNCountry*.
* Generating new userNames based on *firstName* & *lastName*.  

Evidently, loginNameSuggestor expects to receive a person's:
* *SSN*.
* *SSNCountry*.
* *firstName*.
* *lastName*.
* *institution*.

The endpoint shall propose loginNames that can be safely attributed to a new Account.  
A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/loginNameSuggestor).

### roleFinder
Search for a person's active Roles in a institution based on a given userName.

In order for that to happen roleFinder expects to receive a person's:
* *loginName*.
* *institution*.

The endpoint shall decide if a person is a Student, a Member of the Teaching Staff, an Associate, or a combination of the above.  
A complete guide is available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder).

## Error Codes
* **X400**: Missing attribute in JSON Request.
* **X401**: Missing connection details for the institution given.
* **X500**: Wrong/Invalid connection details for the given institution's Views and Data Server.

X is a reference to the endpoint called:
* 1: loginNameValidator
* 2: loginNameSuggestor
* 3: RoleFinder

More information about the error codes and some examples are available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Error-Codes-and-Examples).

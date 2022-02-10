# LoginName-Validity-Checker.
LoginName Validity Checker is an all-around mechanism that is utilized during the University Account creation procedure.

## Endpoints
* ***loginNameValidator/***    
* ***loginNameSuggestor/***    
* ***roleFinder/*** 

The endpoints expect to receive a **JSON** formatted request and respond likewise. Documentation for every endpoint can be found below:

## loginNameValidator/
Examines if a given loginName can be safely attributed to a new Account. 

### Requests
- [Request Attributes.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-Request-Attributes)
- [Request Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-Request-schema)

### Response Code Table

| Response Code | No conflicts | Conflicts Found | improperly created in DS | findExisting flag *false* | previous loginNames *found* | previous loginNames *not found* |
| :----: | :----: | :----: | :----: | :----: | :----: | :----: |
| 1100 | :heavy_check_mark: |  |  |  | :heavy_check_mark: |  |
| 1110 | :heavy_check_mark: |  |  |  |  | :heavy_check_mark: |
| 1120 | :heavy_check_mark: |  |  | :heavy_check_mark: |  |  |
| 1200 |  | :heavy_check_mark: |  |  | :heavy_check_mark: |  |
| 1210 |  | :heavy_check_mark: |  |  |  | :heavy_check_mark: |
| 1220 |  | :heavy_check_mark: |  | :heavy_check_mark: |  |  |
| 1300 |  |  | :heavy_check_mark: | | | |

### Responses
- [Response Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-response-schema)
- [Successful Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-successful-calls)

## loginNameSuggestor/
Responsible for suggesting possible loginNames for a new Account, by:
* Locating previous userNames based on *SSN* & *SSNCountry*.
* Generating new userNames based on *firstName* & *lastName*.

### Requests
- [Request Attributes.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Suggestor-Request-Attributes)
- [Request Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Suggestor-Request-schema)

### Response Code Table
| Response Code | SSN Not given | SSN Not Found | SSN Found | userNameGeneration successful | userNameGeneration failed | 
| :----: | :----: | :----: | :----: | :----: | :----: | 
| 2100 |  |  | :heavy_check_mark: | :heavy_check_mark: |  |
| 2110 |  |  | :heavy_check_mark: |  | :heavy_check_mark: |
| 2200 |  | :heavy_check_mark: |  | :heavy_check_mark: |  |
| 2210 |  | :heavy_check_mark: | |  | :heavy_check_mark: |
| 2300 | :heavy_check_mark: |  | | :heavy_check_mark: |  |
| 2310 | :heavy_check_mark:  |  | |  | :heavy_check_mark: |

### Responses
- [Response Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Suggestor-response-schema)
- [Successful Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Suggestor-successful-calls)

## roleFinder/
Search for a person's active Roles in a institution based on a given userName.
### Requests
- [Request Attributes.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder-Request-Attributes)
- [Request Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder-Request-schema)

### Response Code Table
| Response Code | Student | Teaching Staff | Associate | 
| :----: | :----: | :----: | :----: |
| 3000 |  |  |  |
| 3001 |  |  | :heavy_check_mark: |
| 3010 |  | :heavy_check_mark: |  |
| 3011 |  | :heavy_check_mark: | :heavy_check_mark: |
| 3100 | :heavy_check_mark: |  |  |
| 3101 | :heavy_check_mark: |  | :heavy_check_mark: |
| 3110 | :heavy_check_mark: | :heavy_check_mark: |  |
| 3111 | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |

### Responses
- [Response Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder-response-schema)
- [Successful Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/roleFinder-successful-calls)

## Error Codes
* **X400**: Missing attribute in JSON Request.
* **X401**: Missing connection details for the institution given.
* **X500**: Wrong/Invalid connection details for the given institution's Views and Data Server.

X is a reference to the endpoint called:
* 1: loginNameValidator
* 2: loginNameSuggestor
* 3: RoleFinder

More information about the error codes and some examples are available [here](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Error-Codes-and-Examples).

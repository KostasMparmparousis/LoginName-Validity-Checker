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

### Response Codes

***Response Code Table***
| Response Code | No conflicts | Conflicts Found | improperly created in DS | findExisting flag *false* | previous loginNames *found* | previous loginNames *not found* |
| :----: | :----: | :----: | :----: | :----: | :----: | :----: |
| 100 | :heavy_check_mark: |  |  |  | :heavy_check_mark: |  |
| 110 | :heavy_check_mark: |  |  |  |  | :heavy_check_mark: |
| 120 | :heavy_check_mark: |  |  | :heavy_check_mark: |  |  |
| 200 |  | :heavy_check_mark: |  |  | :heavy_check_mark: |  |
| 210 |  | :heavy_check_mark: |  |  |  | :heavy_check_mark: |
| 220 |  | :heavy_check_mark: |  | :heavy_check_mark: |  |  |
| 300 |  |  | :heavy_check_mark: | | | |

***Error Codes***
* **400**: Missing attribute in JSON Request
* **500**: Missing connection details for the institution given
* **501**: Wrong/Invalid connection details for the given institution's Views
* **502**: Wrong/Invalid connection details for the given institution's Data Servers

### Responses
- [Response Schema.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-response-schema)
- [Incomplete Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-unsuccessful-calls)
- [Successfull Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Validator-successfull-calls)

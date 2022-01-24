# LoginName-Validity-Checker.
LoginName Validity Checker is a API responsible for cross-checking the validity of a given loginName within the confines of an Institution.

## Requests.
The API expects to receive a **JSON** formatted request following the Schema described below.

- [Request Schema Attributes.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Request-properties)
- [Request Schema Validator.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Request-schema)

## Responses
The API will return a **JSON** formatted response as shown below:

- [Response Schema Validator.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Response-schema)

## Response Examples
Two Catalogs containing Incomplete and successfull cals of the API can be found below:
- [Incomplete Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Incomplete-calls)
- [Successfull Calls.](https://github.com/KostasMparmparousis/LoginName-Validity-Checker/wiki/Successfull-calls)

## Response Codes 
| Response Code | No conflicts | Conflicts Found | improperly created in DS | findExisting flag *false* | previous loginNames *found* | previous loginNames *not found* |
| :----: | :----: | :----: | :----: | :----: | :----: | :----: |
| 100 | :heavy_check_mark: |  |  |  | :heavy_check_mark: |  |
| 110 | :heavy_check_mark: |  |  |  |  | :heavy_check_mark: |
| 120 | :heavy_check_mark: |  |  | :heavy_check_mark: |  |  |
| 200 |  | :heavy_check_mark: |  |  | :heavy_check_mark: |  |
| 210 |  | :heavy_check_mark: |  |  |  | :heavy_check_mark: |
| 220 |  | :heavy_check_mark: |  | :heavy_check_mark: |  |  |
| 300 |  |  | :heavy_check_mark: | | | |

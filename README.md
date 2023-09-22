# Sojourner under Sabotage - Server
## Install
- Open the maven project
- Install dependencies `mvn install`
- Rename `example.application.properties` to `application.properties` and add your database credentials
- Run the application `mvn spring-boot:run`

## Test the API
```shell
curl -H "Accept: application/json" \
     -H "Content-Type: application/json" \
     -X POST localhost:8008/api/execute \
     -d "{\"className\":\"Test\",\"sourceCode\":\"public class Test{}\"} \
     -i
```
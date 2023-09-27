# Sojourner under Sabotage - Server
## Install
- Open the maven project
- Install dependencies `mvn install`
- Rename `example.application.properties` to `application.properties` and add your database credentials
- Run the application `mvn spring-boot:run`

## Test the API
```shell
curl localhost:8008/api/hello -i
```


```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST localhost:8008/api/auth -d  "{\"login\":\"user@mail.com\",\"password\":\"1234\"} -i
````

```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d "{\"className\":\"DemoTest\",\"sourceCode\":\"public class DemoTest{@org.junit.Test public void testDemoAddition(){org.junit.Assert.assertEquals(3, Demo.add(2, 1));}}\",\"cutComponentName\":\"Demo\"}" -i -H "Authorization: Bearer insert_valid_token_here" localhost:8008/api/execute
```

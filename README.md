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


```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST localhost:8008/api/auth -d  "{\"login\":\"mail@tim-greller.de\",\"password\":\"1234\"} -i
````

```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d "{\"className\":\"DemoTest\",\"sourceCode\":\"public class DemoTest{@org.junit.Test public void testDemoAddition(){org.junit.Assert.assertEquals(3, Demo.add(1, 2));}}\"}" -i -H "Authorization: Bearer insert_valid_token_here" localhost:8008/api/execute
```
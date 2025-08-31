package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Debug {
    public static void log(String message, String classId, String methodName) {
        log.info(" --- received message in [{}::{}]: {}", classId, methodName, message);

        InstrumentationTracker.trackLog(message, classId, methodName);
    }

    public static void log(int message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(long message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }
    public static void log(float message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(double message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(char message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(boolean message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(byte message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(short message, String classId, String methodName) {
        log(String.valueOf(message), classId, methodName);
    }

    public static void log(Object object, String classId, String methodName) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String message;
        try {
            message = ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            message = Objects.toString(object, "null");
        }
        log(message, classId, methodName);
    }

    public static void log(int[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(long[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(float[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(double[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(char[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(boolean[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(byte[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(short[] array, String classId, String methodName) {
        log(Arrays.toString(array), classId, methodName);
    }

    public static void log(String classId, String methodName) {
        log("\n", classId, methodName);
    }
}

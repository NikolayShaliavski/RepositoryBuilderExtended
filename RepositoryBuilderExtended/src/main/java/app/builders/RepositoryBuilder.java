package app.builders;

import app.constants.BaseConstants;
import app.constants.RepositoryConstants;

import javax.persistence.Id;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RepositoryBuilder {

    private static File repoDirectory;
    private static File repoFile;
    private final static Map<Class<?>, Class<?>> primitivesToWrapper = new HashMap<>();

    static {
        primitivesToWrapper.put(boolean.class, Boolean.class);
        primitivesToWrapper.put(byte.class, Byte.class);
        primitivesToWrapper.put(short.class, Short.class);
        primitivesToWrapper.put(char.class, Character.class);
        primitivesToWrapper.put(int.class, Integer.class);
        primitivesToWrapper.put(long.class, Long.class);
        primitivesToWrapper.put(float.class, Float.class);
        primitivesToWrapper.put(double.class, Double.class);
    }


    public static void createRepository(Map<String, Class> entityClasses, String repositoryPostfix) {
        String postfix = repositoryPostfix.trim().isEmpty() ? RepositoryConstants.REPOSITORY_POSTFIX : repositoryPostfix
                .trim();

        for (Map.Entry<String, Class> classEntry : entityClasses.entrySet()) {
            Class currentClass = classEntry.getValue();
            String className = classEntry.getKey();
            Package packageName = currentClass.getPackage();
            String importEntityPackage = packageName.toString().substring(8);
            repoDirectory = new File(
                    BaseConstants.SOURCE_PATH + "/" +
                            BaseConstants.MAIN_PATH + "/" +
                            RepositoryConstants.REPOSITORY_DIRECTORY_NAME);
            if (!repoDirectory.exists()) {
                repoDirectory.mkdir();
            }
            repoFile = new File(repoDirectory.getAbsolutePath(), className + postfix + ".java");
            if (!repoFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(repoFile, true))) {
                    String mainPath = BaseConstants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                    String repositoryName = className + postfix;
                    if (mainPath.equals(".")) {
                        mainPath = "";
                    }
                    String idType = getIdType(currentClass);
                    if (idType.equals("")) {
                        writer.close();
                        Files.delete(repoFile.toPath());
                        continue;
                    } else {
                        writer.write(String.format(RepositoryConstants.REPOSITORY,
                                mainPath,
                                importEntityPackage,
                                className,
                                repositoryName,
                                className,
                                idType));
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    private static String getIdType(Class currentClass) {
        int idCounter = 0;
        String idType = "";
        Method[] methods = currentClass.getDeclaredMethods();
        boolean hasMethodAnnotation = false;

        for (Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Id.class)) {
                Class type = method.getReturnType();
                if (type.isPrimitive()) {
                    type = primitivesToWrapper.get(type);
                }
                idType = type.getTypeName();
                int indexLastDot = idType.lastIndexOf(".");
                if (indexLastDot != -1) {
                    idType = idType.substring(indexLastDot + 1);
                    hasMethodAnnotation = true;
                    idCounter++;
                }
            }
        }

        if (!hasMethodAnnotation) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    Class type = field.getType();
                    if (type.isPrimitive()) {
                        type = primitivesToWrapper.get(type);
                    }
                    idType = type.getTypeName();
                    int indexLastDot = idType.lastIndexOf(".");
                    if (indexLastDot != -1) {
                        idType = idType.substring(indexLastDot + 1);
                        idCounter++;
                    }
                }
            }
        }

        if (idCounter == 1) {
            return idType;
        } else {
            return "";
        }
    }
}

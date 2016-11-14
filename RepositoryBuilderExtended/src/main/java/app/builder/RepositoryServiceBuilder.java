package app.builder;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RepositoryServiceBuilder {

    private static File repoDirectory;
    private static File repoFile;
    private static File serviceDirectory;
    private static File serviceFile;
    private static Map<String, Class> entityClasses = new HashMap<>();
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
        entityClasses = getEntityFilesRecursive(new File(Constants.SOURCE_PATH), "");
    }

    public static void build() {

        createRepository(entityClasses, "");
    }

    public static void build(String repositoryPostfix) {
        createRepository(entityClasses, repositoryPostfix);
    }

    private static Map<String, Class> getEntityFilesRecursive(File pFile, String packageName) {
        for (File file : pFile.listFiles()) {

            URL[] url = new URL[0];
            try {
                url = new URL[]{file.toURI().toURL()};
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (file.isDirectory()) {
                String path = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                getEntityFilesRecursive(file, path);
            } else {
                int substringLength = file.getName().indexOf('.');
                if (substringLength != -1) {
                    String className = file.getName().substring(0, substringLength);
                    URLClassLoader classLoader = new URLClassLoader(url);
                    Class currentClass = null;
                    try {
                        currentClass = classLoader.loadClass((!packageName.isEmpty() ?
                                (packageName + ".") : ("")) + className);

                        if (currentClass.isAnnotationPresent(SpringBootApplication.class)) {
                            Package currClassPackage = currentClass.getPackage();
                            if (currClassPackage != null) {
                                setMainPath(currentClass.getPackage());
                            }
                        }

                        if (currentClass.isAnnotationPresent(Entity.class)) {
                            if (!entityClasses.containsKey(className)) {
                                entityClasses.put(className, currentClass);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        System.out.println(className + " not found!");
                    }
                }
            }
        }
        return entityClasses;
    }

    private static void createRepository(Map<String, Class> entityClassesMap, String repositorypostfix) {

        String postfix = repositorypostfix.trim().isEmpty() ? Constants.RREPOSITORY_POSTFIX : repositorypostfix.trim();

        for (Map.Entry<String, Class> classEntry : entityClassesMap.entrySet()) {
            Class currentClass = classEntry.getValue();
            String className = classEntry.getKey();
            Package packageName = currentClass.getPackage();
            String importEntityPackage = packageName.toString().substring(8);
            repoDirectory = new File(
                    Constants.SOURCE_PATH + "/" + Constants.MAIN_PATH + "/" + Constants.REPOSITORY_DIRECTORY_NAME);
            if (!repoDirectory.exists()) {
                repoDirectory.mkdir();
            }
            repoFile = new File(repoDirectory.getAbsolutePath(), className + postfix + ".java");
            if (!repoFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(repoFile, true))) {
                    StringBuilder builder = new StringBuilder(32);
                    String mainPath = Constants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                    if (mainPath.equals(".")) {
                        mainPath = "";
                    }
                    builder.append(Constants.PACKAGE + mainPath + Constants.REPOSITORY_DIRECTORY_NAME + ";");
                    builder.append(System.lineSeparator());
                    builder.append(System.lineSeparator());
                    builder.append(Constants.REPOSITORY_IMPORT);
                    builder.append(System.lineSeparator());
                    builder.append(Constants.JPA_IMPORT);
                    builder.append(System.lineSeparator());
                    builder.append("import ").append(importEntityPackage).append(".").append(className).append(";");
                    builder.append(System.lineSeparator());
                    builder.append(System.lineSeparator());
                    builder.append(Constants.REPOSITORY_ANNOTATION);
                    builder.append(System.lineSeparator());
                    String idType = getIdType(currentClass);

                    if (idType.equals("")) {
                        writer.close();
                        Files.delete(repoFile.toPath());
                        continue;
                    } else {
                        builder.append(String.format(Constants.REPOSITORY_INTERFACE, className + postfix,
                                className, idType));
                        builder.append(System.lineSeparator());
                        builder.append(Constants.CLOSE_BRACKET);
                        writer.write(builder.toString());
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            createService(currentClass);
            createServiceImplementations(currentClass);
        }
    }

    private static void createService(Class currentClass) {
        String className = currentClass.getSimpleName();
        serviceDirectory = new File(
                Constants.SOURCE_PATH + "/" + Constants.MAIN_PATH + "/" + Constants.SERVICE_DIRECTORY_NAME);
        if (!serviceDirectory.exists()) {
            serviceDirectory.mkdir();
        }
        serviceFile = new File(serviceDirectory.getAbsolutePath(), className + Constants.SERVICE_POSTFIX + ".java");
        if (!serviceFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(serviceFile, true))) {
                StringBuilder builder = new StringBuilder();
                String mainPath = Constants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                if (mainPath.equals(".")) {
                    mainPath = "";
                }
                builder.append(Constants.PACKAGE + mainPath + Constants.SERVICE_DIRECTORY_NAME + ";");
                builder.append(System.lineSeparator());
                builder.append(System.lineSeparator());
                builder.append(String.format(
                        Constants.SERVICE_INTERFACE, className + Constants.SERVICE_POSTFIX));
                builder.append(System.lineSeparator());
                builder.append(Constants.CLOSE_BRACKET);
                writer.write(builder.toString());
                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private static void createServiceImplementations(Class currentClass) {
        String className = currentClass.getSimpleName();
        String repoPath = Constants.MAIN_PATH + ".";
        if (repoPath.equals(".")) {
            repoPath = "repositories.";
        } else {
            repoPath += "repositories.";
        }
        String importRepositoryPackage =
                "import " + repoPath + repoFile.getName().replace(".java", "") + ";";
        importRepositoryPackage = importRepositoryPackage.replaceAll("\\/","\\.");
        String servicePath = Constants.MAIN_PATH + ".";
        if (servicePath.equals(".")) {
            servicePath = "services.";
        } else {
            servicePath += "services.";
        }
        String importServicePackage =
                "import " + servicePath + serviceFile.getName().replace(".java", "") + ";";
        importServicePackage = importServicePackage.replaceAll("\\/","\\.");
        File serviceImplDirectory = new File(
                serviceDirectory.getAbsolutePath() + "/" + Constants.SERVICE_IMPL_DIRECTORY_NAME);
        if (!serviceImplDirectory.exists()) {
            serviceImplDirectory.mkdir();
        }
        File serviceImplFile = new File(
                serviceImplDirectory.getAbsolutePath(), className + Constants.SERVICE_IMPL_POSTFIX + ".java");
        if (!serviceImplFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(serviceImplFile, true))) {
                StringBuilder builder = new StringBuilder();
                String mainPath = Constants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                if (mainPath.equals(".")) {
                    mainPath = "";
                }
                builder.append(Constants.PACKAGE + mainPath +
                        "services" + "." + Constants.SERVICE_IMPL_DIRECTORY_NAME + ";");
                builder.append(System.lineSeparator());
                builder.append(System.lineSeparator());
                builder.append(Constants.AUTOWIRED_IMPORT);
                builder.append(System.lineSeparator());
                builder.append(Constants.SERVICE_IMPORT);
                builder.append(System.lineSeparator());
                builder.append(Constants.TRANSACTIONAL_IMPORT);
                builder.append(System.lineSeparator());
                builder.append(importRepositoryPackage);
                builder.append(System.lineSeparator());
                builder.append(importServicePackage);
                builder.append(System.lineSeparator());
                builder.append(System.lineSeparator());
                builder.append(Constants.SERVICE_ANNOTATION);
                builder.append(System.lineSeparator());
                builder.append(Constants.SERVICE_TRANSACTIONAL);
                builder.append(System.lineSeparator());
                builder.append(String.format(
                        Constants.SERVICE_IMPL,
                        className + Constants.SERVICE_IMPL_POSTFIX,
                        serviceFile.getName().replace(".java", "")));
                builder.append(System.lineSeparator());
                builder.append(System.lineSeparator());
                builder.append(Constants.AUTOWIRED_ANNOTATION);
                builder.append(System.lineSeparator());
                Character repoFieldFirstLetter = repoFile.getName().charAt(0);
                String repoVariableName =
                        Character.toLowerCase(repoFieldFirstLetter) +
                        repoFile.getName().substring(1).replace(".java", "");
                builder.append(String.format(
                        Constants.SERVICE_REPO_FIELD, repoFile.getName().replace(".java", ""), repoVariableName));
                builder.append(System.lineSeparator());
                builder.append(System.lineSeparator());
                builder.append(Constants.CLOSE_BRACKET);
                writer.write(builder.toString());
                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println(e);
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

    private static void setMainPath(Package packageSource) {
        String packageName = packageSource.toString().substring(8);
        Constants.MAIN_PATH = packageName.replaceAll("\\.", "/");
    }
}

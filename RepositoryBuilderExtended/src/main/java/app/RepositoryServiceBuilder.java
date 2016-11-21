package app;

import app.builders.RepositoryBuilder;
import app.builders.ServiceBuilder;
import app.constants.BaseConstants;
import app.constants.RepositoryConstants;
import app.strategies.Strategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class RepositoryServiceBuilder {

    private static File repoDirectory;
    private static File repoFile;
    private static File serviceDirectory;
    private static File serviceFile;
    private static Map<String, Class> entityClasses = new HashMap<>();
    private static Map<String, Class> repositoriesEntities = new HashMap<>();
    private final static Map<Class<?>, Class<?>> primitivesToWrapper = new HashMap<>();

    private static void initialize() {
        primitivesToWrapper.put(boolean.class, Boolean.class);
        primitivesToWrapper.put(byte.class, Byte.class);
        primitivesToWrapper.put(short.class, Short.class);
        primitivesToWrapper.put(char.class, Character.class);
        primitivesToWrapper.put(int.class, Integer.class);
        primitivesToWrapper.put(long.class, Long.class);
        primitivesToWrapper.put(float.class, Float.class);
        primitivesToWrapper.put(double.class, Double.class);
        getEntityFilesRecursive(new File(BaseConstants.SOURCE_PATH), "");
    }

    public static void build(Strategy strategy) {
        build(strategy, "");
    }

    public static void build(Strategy strategy, String repositoryPostfix) {
        initialize();
        if (strategy.equals(Strategy.REPOSITORY_ONLY)) {
            RepositoryBuilder.createRepository(entityClasses, repositoryPostfix);
        } else {
            String postfix =
                    repositoryPostfix.equals("") ? RepositoryConstants.REPOSITORY_POSTFIX : repositoryPostfix;
            addRepositoryNames(postfix);
            RepositoryBuilder.createRepository(entityClasses, repositoryPostfix);
            ServiceBuilder.createService(repositoriesEntities);
        }
    }

    private static void getEntityFilesRecursive(File pFile, String packageName) {
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
    }


    private static void setMainPath(Package packageSource) {
        String packageName = packageSource.toString().substring(8);
        BaseConstants.MAIN_PATH = packageName.replaceAll("\\.", "/");
    }

    private static void addRepositoryNames(String repositoryPostfix) {
        for (Map.Entry<String, Class> entityClass : entityClasses.entrySet()) {
            if (hasIdAnnotation(entityClass.getValue())) {
                String repoName = entityClass.getKey() + repositoryPostfix;
                if (!repositoriesEntities.containsKey(repoName)) {
                    repositoriesEntities.put(repoName, entityClass.getValue());
                }
            }
        }
    }

    private static boolean hasIdAnnotation(Class entityClass) {
        Method[] methods = entityClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Id.class)) {
                return true;
            }
        }
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                return true;
            }
        }
        return false;
    }
}

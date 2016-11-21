package app.builders;

import app.constants.BaseConstants;
import app.constants.RepositoryConstants;
import app.constants.ServiceConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ServiceBuilder {

    private static File serviceDirectory;
    private static File serviceFile;

    public static void createService(Map<String, Class> repositoriesEntities) {
        for (Map.Entry<String,Class> repoEntityEntry : repositoriesEntities.entrySet()) {
            String className = repoEntityEntry.getValue().getSimpleName();
            serviceDirectory = new File(
                    BaseConstants.SOURCE_PATH + "/" +
                    BaseConstants.MAIN_PATH + "/" + ServiceConstants.SERVICE_DIRECTORY_NAME);
            if (!serviceDirectory.exists()) {
                serviceDirectory.mkdir();
            }
            serviceFile = new File(
                    serviceDirectory.getAbsolutePath(), className +
                    ServiceConstants.SERVICE_POSTFIX + ".java");
            if (!serviceFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(serviceFile, true))) {
                    String mainPath = BaseConstants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                    String serviceName = className + ServiceConstants.SERVICE_POSTFIX;
                    if (mainPath.equals(".")) {
                        mainPath = "";
                    }
                    writer.write(String.format(ServiceConstants.SERVICE,
                            mainPath,
                            serviceName));
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            createServiceImplementation(repoEntityEntry.getKey(), repoEntityEntry.getValue());
        }
    }

    private static void createServiceImplementation(String repoName, Class entity) {
        String className = entity.getSimpleName();
        String repoPath = BaseConstants.MAIN_PATH + ".";
        if (repoPath.equals(".")) {
            repoPath = RepositoryConstants.REPOSITORY_DIRECTORY_NAME + ".";
        } else {
            repoPath += RepositoryConstants.REPOSITORY_DIRECTORY_NAME + ".";
        }
        String importRepositoryPackage = repoPath + repoName;
        importRepositoryPackage = importRepositoryPackage.replaceAll("\\/","\\.");
        String servicePath = BaseConstants.MAIN_PATH + ".";
        if (servicePath.equals(".")) {
            servicePath = ServiceConstants.SERVICE_DIRECTORY_NAME + ".";
        } else {
            servicePath += ServiceConstants.SERVICE_DIRECTORY_NAME + ".";
        }
        String importServicePackage = servicePath + serviceFile.getName().replace(".java", "");
        importServicePackage = importServicePackage.replaceAll("\\/","\\.");
        File serviceImplDirectory = new File(
                serviceDirectory.getAbsolutePath() + "/" +
                ServiceConstants.SERVICE_IMPL_DIRECTORY_NAME);
        if (!serviceImplDirectory.exists()) {
            serviceImplDirectory.mkdir();
        }
        File serviceImplFile = new File(
                serviceImplDirectory.getAbsolutePath(), className +
                ServiceConstants.SERVICE_IMPL_POSTFIX + ".java");
        if (!serviceImplFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(serviceImplFile, true))) {
                String mainPath = BaseConstants.MAIN_PATH.replaceAll("\\/", "\\.") + ".";
                if (mainPath.equals(".")) {
                    mainPath = "";
                }
                Character repoFieldFirstLetter = repoName.charAt(0);
                String repoVariableName =
                        Character.toLowerCase(repoFieldFirstLetter) +
                                repoName.substring(1).replace(".java", "");
                writer.write(String.format(ServiceConstants.SERVICE_IMPL,
                        mainPath,
                        importRepositoryPackage,
                        importServicePackage,
                        className + ServiceConstants.SERVICE_IMPL_POSTFIX,
                        serviceFile.getName().replace(".java", ""),
                        repoName,
                        repoVariableName));
                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

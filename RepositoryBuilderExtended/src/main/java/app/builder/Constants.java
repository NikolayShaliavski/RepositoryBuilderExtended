package app.builder;

public class Constants {

    public final static String SOURCE_CODE_PATH = "/src/main/java";
    public final static String REPOSITORY_DIRECTORY_NAME = "repositories";
    public final static String PROJECT_PATH = System.getProperty("user.dir");
    public final static String SOURCE_PATH = PROJECT_PATH + SOURCE_CODE_PATH;

    public final static String REPOSITORY_IMPORT = "import org.springframework.stereotype.Repository;";
    public final static String JPA_IMPORT = "import org.springframework.data.jpa.repository.JpaRepository;";
    public final static String REPOSITORY_ANNOTATION = "@Repository";
    public final static String REPOSITORY_INTERFACE = "public interface %s extends JpaRepository<%s,%s> {";
    public final static String CLOSE_BRACKET = "}";
    public final static String PACKAGE = "package ";
    public final static String RREPOSITORY_POSTFIX = "Repository";
    public static String MAIN_PATH = "";

    public static final String SERVICE_DIRECTORY_NAME = "services";
    public static final String SERVICE_INTERFACE = "public interface %s {";
    public static final String SERVICE_POSTFIX = "Service";

    public static final String SERVICE_IMPL_DIRECTORY_NAME = "servicesImpl";
    public static final String AUTOWIRED_IMPORT =
            "import org.springframework.beans.factory.annotation.Autowired;";
    public static final String SERVICE_IMPORT = "import org.springframework.stereotype.Service;";
    public static final String TRANSACTIONAL_IMPORT = "import org.springframework.transaction.annotation.Transactional;";
    public static final String SERVICE_ANNOTATION = "@Service";
    public static final String SERVICE_TRANSACTIONAL = "@Transactional";
    public static final String AUTOWIRED_ANNOTATION = "    @Autowired";
    public static final String SERVICE_IMPL = "public class %s implements %s {";
    public static final String SERVICE_REPO_FIELD = "    private %s %s;";
    public static final String SERVICE_IMPL_POSTFIX = "ServiceImpl";
}

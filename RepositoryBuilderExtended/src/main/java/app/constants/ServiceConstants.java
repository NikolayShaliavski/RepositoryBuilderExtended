package app.constants;

public class ServiceConstants {

    public final static String SERVICE = "package %sservices;\n" +
            "\n" +
            "public interface %s {\n" +
            "}";

    public final static String SERVICE_DIRECTORY_NAME = "services";
    public final static String SERVICE_POSTFIX = "Service";

    public final static String SERVICE_IMPL  = "package %sservices.servicesImpl;\n" +
            "\n" +
            "import org.springframework.beans.factory.annotation.Autowired;\n" +
            "import org.springframework.stereotype.Service;\n" +
            "import org.springframework.transaction.annotation.Transactional;\n" +
            "import %s;\n" +
            "import %s;\n" +
            "\n" +
            "@Service\n" +
            "@Transactional\n" +
            "public class %s implements %s {\n" +
            "\n" +
            "    @Autowired\n" +
            "    private %s %s;\n" +
            "\n" +
            "}";

    public final static String SERVICE_IMPL_DIRECTORY_NAME = "servicesImpl";
    public final static String SERVICE_IMPL_POSTFIX = "ServiceImpl";
}

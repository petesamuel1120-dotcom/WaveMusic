import java.nio.file.*;
import org.jetbrains.kotlin.cli.jvm.compiler.*;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;

class KotlinSyntaxCheck {
    public static void main(String[] paths) throws Exception {
        var disposable = Disposer.newDisposable();
        boolean failed = false;
        try {
            var config = new CompilerConfiguration();
            org.jetbrains.kotlin.cli.FrontendConfigurationKeysKt.setExtensionsStorage(config, new org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage());
            var env = KotlinCoreEnvironment.createForProduction(disposable, config, EnvironmentConfigFiles.JVM_CONFIG_FILES);
            var factory = new KtPsiFactory(env.getProject(), false);
            for (String path : paths) {
                var file = factory.createFile(Path.of(path).getFileName().toString(), Files.readString(Path.of(path)).replace("\r\n", "\n").replace("\r", "\n"));
                var errors = PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class);
                if (errors.isEmpty()) System.out.println("Syntax OK: " + file.getName());
                for (var error : errors) {
                    failed = true;
                    int line = file.getText().substring(0, error.getTextOffset()).split("\n", -1).length;
                    System.out.println(path + ":" + line + ": " + error.getErrorDescription());
                }
            }
        } finally { Disposer.dispose(disposable); }
        if (failed) System.exit(1);
    }
}
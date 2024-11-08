import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class GitCommitValueSource : ValueSource<String, ValueSourceParameters.None> {

    @Inject
    abstract fun getExecOperations(): ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        getExecOperations().exec {
            commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
            standardOutput = output
        }
        return String(output.toByteArray(), Charset.defaultCharset()).trim()
    }
}

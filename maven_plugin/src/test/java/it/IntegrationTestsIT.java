package it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import io.github.chains_project.maven_lockfile.data.LockFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

@MavenJupiterExtension
public class IntegrationTestsIT extends AbstractMojoTestCase {
    @MavenTest
    public void simpleProject(MavenExecutionResult result) throws Exception {
        // contract: an empty project should generate an empty lock file
        assertThat(result).isSuccessful();
        Path lockFilePath = getLockFile(result);
        assertThat(lockFilePath).exists();
        var lockFile = LockFile.readLockFile(lockFilePath);
        assertThat(lockFile.getDependencies()).isEmpty();
    }

    @MavenTest
    public void singleDependency(MavenExecutionResult result) throws Exception {
        // contract: an empty project should generate an empty lock file
        assertThat(result).isSuccessful();
        Path lockFilePath = getLockFile(result);
        assertThat(lockFilePath).exists();
        var lockFile = LockFile.readLockFile(lockFilePath);
        assertThat(lockFile.getDependencies()).hasSize(1);
        var junitDep = lockFile.getDependencies().get(0);
        assertThat(junitDep.getArtifactId()).extracting(v -> v.getValue()).isEqualTo("spoon-core");
        assertThat(junitDep.getGroupId()).extracting(v -> v.getValue()).isEqualTo("fr.inria.gforge.spoon");
        assertThat(junitDep.getVersion()).extracting(v -> v.getValue()).isEqualTo("10.3.0");
        assertThat(junitDep.getChecksum()).isEqualTo("d94722f53c95e49d8c1628708e3a168dc748e956");
    }

    @MavenTest
    public void singleDependencyCheckCorrect(MavenExecutionResult result) throws Exception {
        // contract: an empty project should generate an empty lock file
        assertThat(result).isSuccessful();
        Path lockFilePath = getLockFile(result);
        assertThat(lockFilePath).exists();
        var lockFile = LockFile.readLockFile(lockFilePath);
        assertThat(lockFile.getDependencies()).hasSize(1);
        var junitDep = lockFile.getDependencies().get(0);
        assertThat(junitDep.getArtifactId()).extracting(v -> v.getValue()).isEqualTo("junit-jupiter-api");
        assertThat(junitDep.getGroupId()).extracting(v -> v.getValue()).isEqualTo("org.junit.jupiter");
        assertThat(junitDep.getVersion()).extracting(v -> v.getValue()).isEqualTo("5.9.2");
        assertThat(junitDep.getChecksum()).isEqualTo("fed843581520eac594bc36bb4b0f55e7b947dda9");
    }

    @MavenTest
    public void singleDependencyCheckMustFail(MavenExecutionResult result) throws Exception {
        // contract: a changed dependency should fail the build.
        // we changed the group id of "groupId": "org.opentest4j", to "groupId": "org.opentest4j5",
        assertThat(result).isFailure();
    }

    @MavenTest
    public void pluginProject(MavenExecutionResult result) throws Exception {
        assertThat(result).isSuccessful();
        Path lockFilePath = getLockFile(result);
        assertThat(lockFilePath).exists();
        var lockFile = LockFile.readLockFile(lockFilePath);
        assertThat(lockFile.getMavenPlugins()).isNotEmpty();
    }

    @MavenTest
    public void freezeJunit(MavenExecutionResult result) throws Exception {
        assertThat(result).isSuccessful();
        var path = Files.find(
                        result.getMavenProjectResult().getTargetBaseDirectory(),
                        Integer.MAX_VALUE,
                        (u, v) -> u.getFileName().toString().contains("pom.xml"))
                .findAny()
                .orElseThrow();
        var pom = Files.readString(path);
        assertThat(pom).contains("<groupId>org.junit.jupiter</groupId>");
        assertThat(pom).contains("<artifactId>junit-jupiter-api</artifactId>");
        assertThat(pom).contains("<version>5.9.2</version>");
    }

    private Path getLockFile(MavenExecutionResult result) throws IOException {
        return Files.find(
                        result.getMavenProjectResult().getTargetBaseDirectory(),
                        Integer.MAX_VALUE,
                        (v, u) -> v.getFileName().toString().contains("lockfile.json"))
                .findFirst()
                .orElseThrow();
    }
}

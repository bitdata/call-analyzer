package bitdata.code.util;

import bitdata.code.entity.SourceLine;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitUtil {

    private static final Pattern pattern = Pattern.compile(".*src/main/java/");

    public static List<SourceLine> getDirtyLines(String gitDir, Date commitTime) throws IOException, GitAPIException {
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        Repository repository = fileRepositoryBuilder.setGitDir(new File(gitDir))
                .readEnvironment()
                .findGitDir()
                .build();
        Git git = new Git(repository);
        RevCommit newCommit = null;
        RevCommit oldCommit = null;
        for (RevCommit commit : git.log().call()) {
            if (newCommit == null) {
                newCommit = commit;
            }
            Date date = new Date(commit.getCommitTime() * 1000L);
            if (date.before(commitTime)) {
                oldCommit = commit;
                break;
            }
        }
        if (newCommit == null || oldCommit == null) {
            return null;
        }
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldCommit.getTree().getId());
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newCommit.getTree().getId());
        return parseSourceLines(repository, git, oldTreeIter, newTreeIter);
    }

    private static List<SourceLine> parseSourceLines(Repository repository, Git git, CanonicalTreeParser oldTreeIter, CanonicalTreeParser newTreeIter) throws GitAPIException, IOException {
        List<SourceLine> list = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(repository);
        List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
        for (DiffEntry diff : diffs) {
            if (diff.getNewPath().endsWith(".java")) {
                Matcher matcher = pattern.matcher(diff.getNewPath());
                String path = matcher.replaceAll("");
                df.format(diff);
                FileHeader fileHeader = df.toFileHeader(diff);
                for (HunkHeader hunkHeader : fileHeader.getHunks()) {
                    for (int i = 0; i < hunkHeader.getNewLineCount(); i++) {
                        list.add(new SourceLine(path, hunkHeader.getNewStartLine() + i));
                    }
                }
            }
        }
        return list;
    }
}

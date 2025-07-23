package de.jonihoffi.plugins.enhavo;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class MigrationToolWindow {
    private final Project project;
    private final JPanel contentPanel;
    private DefaultTreeModel treeModel;
    private final String migrationsPath;

    public MigrationToolWindow(Project project) {
        this.project = project;
        this.contentPanel = new SimpleToolWindowPanel(true);
        this.migrationsPath = loadMigrationsPath();
        setupUI();
        reloadMigrations();
    }

    private void setupUI() {
        JButton reloadButton = new JButton("Reload");
        reloadButton.addActionListener(e -> reloadMigrations());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.add(reloadButton);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        JTree blockTree = createMigrationsTree();
        JScrollPane scrollPane = new JBScrollPane(blockTree);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public JComponent getContent() {
        return contentPanel;
    }

    private JTree createMigrationsTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Migrations");
        treeModel = new DefaultTreeModel(root);

        JTree tree = new Tree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null || !node.isLeaf()) return;

            String migrationName = node.getUserObject().toString();
            openClassFile(migrationName);
        });

        return tree;
    }

    private void addFilesToTree(File dir, DefaultMutableTreeNode node) {
        File[] phpFiles = dir.listFiles((d, name) -> name.endsWith(".php"));
        if (phpFiles != null) {
            // Sort files by date in descending order (newest first)
            Arrays.sort(phpFiles, Comparator.comparing(this::extractDateFromFilename, Comparator.nullsLast(Date::compareTo)).reversed());

            for (File file : phpFiles) {
                String fileNameWithoutExtension = file.getName().replace(".php", "");
                node.add(new DefaultMutableTreeNode(fileNameWithoutExtension));
            }
        }
    }

    private Date extractDateFromFilename(File file) {
        String fileName = file.getName().replace(".php", "");
        String dateString = fileName.replaceAll("\\D", "");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openClassFile(String migrationName) {
        new Task.Backgroundable(project, "Opening Migration File") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String filePath = migrationsPath + "/" + migrationName + ".php";
                VirtualFile virtualFile = ReadAction.compute(() -> VirtualFileManager.getInstance().findFileByUrl("file://" + filePath));
                if (virtualFile != null) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                        if (psiFile != null) {
                            NavigationUtil.openFileWithPsiElement(psiFile, true, true);
                        }
                    });
                }
            }
        }.queue();
    }

    private void reloadMigrations() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        File migrationsDir = new File(migrationsPath);
        if (migrationsDir.exists() && migrationsDir.isDirectory()) {
            addFilesToTree(migrationsDir, root);
        }

        treeModel.reload();
    }

    private String loadMigrationsPath() {
        String yamlPath = project.getBasePath() + "/config/packages/doctrine_migrations.yaml";
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(yamlPath)) {
            Map<String, Object> config = yaml.load(inputStream);
            Map<String, Object> doctrineMigrations = (Map<String, Object>) config.get("doctrine_migrations");
            Map<String, String> migrationsPaths = (Map<String, String>) doctrineMigrations.get("migrations_paths");
            if (migrationsPaths == null) {
                Map<String, String> migrations = (Map<String, String>) config.get("doctrine_migrations");
                String path = migrations.get("dir_name");
                return path.replace("%kernel.project_dir%", project.getBasePath());
            } else {
                String path = migrationsPaths.get("App\\Migration");
                return path.replace("%kernel.project_dir%", project.getBasePath());
            }
        } catch (IOException | ClassCastException e) {
            return project.getBasePath() + "/src/Migrations";
        }
    }
}

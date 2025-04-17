import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;

/*
像DOS一样，MiniDOS也有工作目录。工作目录是一个文件夹。定位文件可以用绝对路径，也可以用相对路径。相对路径是相对于当前工作路径的。
系统启动之处，工作路径就是文件 MiniDOS.class 所在目录。系统启动时，要提示用户“请直接输入命令，或输入help查询所有命令。”。
对工作目录有特殊要求：（1）不能删除工作目录，更不能删除工作目录的任意一级祖先目录。（2）不能移动工作目录或者其任意一级祖先目录到其它文件夹中。
当前工作目录是循环出现在交互界面中的，直至用户退出系统。
*/

public class MiniDOS {

    private File currentDir;

    public static void main(String[] args) {
        MiniDOS dos = new MiniDOS();
        dos.run();
    }

    public MiniDOS() {
        // 获取当前 .class 所在目录
        ClassLoader classLoader = MiniDOS.class.getClassLoader();
        URL location = classLoader.getResource("");
        this.currentDir = new File(location.getPath());
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("欢迎进入迷你文件操作系统!" + "\n" + "请直接输入命令，或输入help查询所有命令");

        while (true) {
            System.out.print("\n"); // 回车空行
            System.out.print(this.currentDir.getPath() + ">");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            Pattern pattern = Pattern.compile("(\\S+)");
            Matcher matcher = pattern.matcher(input);

            ArrayList<String> tokens = new ArrayList<>();

            while (matcher.find()) {
                tokens.add(matcher.group(1));
            }

            String command = tokens.get(0).toLowerCase();

            try {
                switch (command) {
                    case "help":
                        handleHelp(); // finished
                        break;
                    case "cd":
                        handleCd(tokens); // finished
                        break;
                    case "dir":
                        handleDir(tokens); // finished
                        break;
                    case "md":
                        handleMd(tokens); // to be done
                        break;
                    case "rn":
                        handleRn(tokens);
                        break;
                    case "copy":
                        handleCopy(tokens, this.currentDir);
                        break;
                    case "move":
                        handleMove(tokens);
                        break;
                    case "del":
                        handleDel(tokens, scanner, this.currentDir);
                        break;
                    case "exit":
                        System.out.println("再见！");
                        return;
                    default:
                        System.out.println("不能正确解析命令。注意命令和参数之间、参数与参数之间至少有一个空格或制表符。请输入help查询所有命令。");
                }
            } catch (Exception e) {
                System.out.println("执行命令时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleHelp() {
        System.out.println("""
                迷你文件操作系统共有9个命令，不区分大小写，其参数分为无参、单参和双参3种情况。
                命令前后可以有任意多的空白，命令和参数之间、参数与参数之间可以有任意多的空白，但至少有一个空格或制表符。
                help --列出所有命令及描述，无参。
                cd-更改当前工作目录，单参，参数分为相对路径和绝对路径两种。两个点 .. 表示上级目录，反斜杠 \\ 表示根目录。单参表示相对路径或绝对路径。dir-·显示目录中的内容，包括文件/文件夹类型、文件总数、文件字节总数等。无参或单参：无参表示针对当前目录；单参表示相对文件夹或绝对文件夹。
                md --在当前工作目录下创建文件夹。单参，表示新创建的文件夹名称，要符合操作系统文件夹名称规范。
                rn --把当前工作目录下的一个直接子目录或子文件重新命名。双参，分别表示更改前名称和更改后名称。
                copy --拷贝文件。单参或双参。单参表示把文件或文件夹拷贝至当前工作目 录。双参，第1参数表示源路径，文件和文件夹均可；第2参数表示目的文件夹路径。双参均可以是相对路径，也可以是绝对路径。不执行逻辑不通的拷贝，包括已存或无限递归等情况。del --删除文件或文件夹。单参，表示要级联删除的文件或文件夹。不能删除当前工作文件夹，也不能删除当前工作文件夹的任意祖先。这个命令在执行之前需要证的用户同意。
                move--把文件或文件夹移走至目的文件夹。单参或双参。单参表示把文件或文件夹移至当前工作目录。双参表示把第1参文件或文件夹移至第2参文件夹。不能移动当前工作文件夹及其祖先；不能出现已存或无限递归情况。exit --退出迷你文件操作系统。无参。
                """);
    }

    private void handleCd(ArrayList<String> tokens) throws IOException {
        // TODO: 实现cd命令，支持 ..、\、相对路径、绝对路径
        // no parameter
        if (tokens.size() == 1) {
            System.out.println("更改当前工作目录的命令cd后面需要加单参数：表示相对路径或绝对路径；但也可以是双点..表示返回上级目录");
            return;
        }

        File target_dir = new_file(this.currentDir, tokens.get(1));
        // only accept one parameter
        if (tokens.size() > 2) {
            System.out.println("此命令只接收单参！");
        }

        // it doesn't exist
        else if (!target_dir.exists()) {
            System.out.println("目标目录不存在");
        }

        // it's not a directory
        else if (!target_dir.isDirectory()) {
            System.out.println("目标目录不是文件夹，而是文件");
        }

        // it's a root
        else if (tokens.get(1).equals("/")) {
            this.currentDir = target_dir;
        }

        // it's a father_dir
        else if (tokens.get(1).equals("..")) {
            File father_dir = this.currentDir.getParentFile();

            // not a root itself
            if (father_dir != null) {
                this.currentDir = father_dir;
            }
            // if it's a root, don't do anything
        }

        // it's a current_dir notation
        else if (tokens.get(1).equals(".")) {
            ; // we don't do anything
        }

        // it's a valid directory
        else {
            this.currentDir = target_dir;
        }
    }

    private void handleDir(ArrayList<String> tokens) throws IOException {
        ArrayList<ArrayList<Object>> lines = new ArrayList<>();
        String firstline = new String("文件(夹)名称     类型(文件/文件夹)     最大深度     内含文件夹总数     内含文件总数     内含文件大小之和");
        // no parameter
        // 名称、是文件还是文件夹、最大深度、内含文件夹总数、内含文件总数、内含文件大小之和
        if (tokens.size() == 1) {
            System.out.println(firstline);
            this.calculate_dir(lines, this.currentDir);
            for (ArrayList<Object> line : lines) {
                for (Object o : line) {
                    System.out.print(o + "     ");
                }
                System.out.println();
            }
        } else if (tokens.size() > 2) {
            System.out.println("命令dir用于显示文件或文件夹的信息，分为无参、单参两种情况。");
        } else {
            File parameter_file = new_file(this.currentDir, tokens.get(1));
            System.out.println(firstline);
            // 如果是文件夹
            if (parameter_file.isDirectory()) {
                this.calculate_dir(lines, parameter_file);
            } else if (parameter_file.isFile()) {
                this.calculate_file(lines, parameter_file);
            }
            for (ArrayList<Object> line : lines) {
                for (Object o : line) {
                    System.out.print(o + "     ");
                }
                System.out.println();
            }
        }
    }

    private void handleMd(ArrayList<String> tokens) throws IOException {
        // 严格要求单参
        if (tokens.size() != 2) {
            System.out.println("命令md用于在当前工作目录下创建一个子目录，其为单参命令");
        } else {
            // 确保路径格式正确
            if (tokens.get(1).matches(".*[/:\\\\].*")) {
                System.out.println("将要创建的文件夹必须在当前工作目录下，且不能含有正斜杠、反斜杠和冒号。");
                return;
            }

            // 创建文件夹
            File new_dir = new_file(this.currentDir, tokens.get(1));
            if (new_dir.mkdir()) {
                System.out.println("新文件夹创建成功。");
            } else {
                System.out.println("该文件夹已存在。");
            }
        }
    }

    private void handleRn(ArrayList<String> tokens) throws IOException {
        // 要求双参
        if (tokens.size() != 3) {
            System.out.println("命令rn用于把当前工作目录中的一个文件或文件夹重命名。是一个双参命令，第1个参数表示原文件（夹）名称，第2个参数表示命名后的名称。");
        } else {
            // 更改的路径得符合要求
            if (tokens.get(2).matches(".*[/:\\\\].*")) {
                System.out.println("将要创建的文件夹必须在当前工作目录下，且不能含有正斜杠、反斜杠和冒号。");
                return;
            }

            File old_item = new_file(this.currentDir, tokens.get(1));
            File new_item = new_file(this.currentDir, tokens.get(2));

            if (!old_item.exists()) {
                System.out.println("源文件不存在。");
            } else if (new_item.exists()) {
                System.out.println("修改后的名称已经存在。");
            } else {
                if (old_item.renameTo(new_item)) {
                    System.out.println("修改名称成功");
                }
            }
        }
    }

    private void handleCopy(ArrayList<String> tokens, File cur_dir) throws IOException {
        // 显示用法
        if (tokens.size() == 1) {
            System.out.println("命令copy实现文件和文件夹的拷贝，是单参或双参的命令。");
        }

        // 不符合参数条件
        else if (tokens.size() > 3) {
            System.out.println("命令copy实现文件和文件夹的拷贝，是单参或双参的命令。");
        }

        // 单参
        else if (tokens.size() == 2) {
            // 相对路径 绝对路径都可以
            File cur_file = new_file(cur_dir, tokens.get(1));

            // 文件不存在
            if (!cur_file.exists()) {
                System.out.println("该文件或文件夹不存在。");
                throw new IOException("该文件或文件夹不存在");
            }

            // 命名冲突
            if (file_exists(cur_file, cur_dir)) {
                System.out.println("想要拷贝的文件或文件夹在当前工作目录中已经存在。");
                throw new IOException("想要拷贝的文件或文件夹在当前工作目录中已经存在");
            }

            // 逻辑爆炸
            else if (this.path_explode(cur_file)) {
                System.out.println("拷贝逻辑爆炸。");
                throw new IOException("逻辑文件爆炸");
            }

            // 开始复制
            else {
                if (cur_file.isFile()) {
                    try {
                        copyFile(cur_file, new_file(cur_dir, cur_file.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("文件拷贝成功。");
                } else if (cur_file.isDirectory()) {
                    try {
                        copyDirectory(cur_file, cur_dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("文件拷贝成功。");
                }
            }
        }

        // 双参
        else {
            File src = new_file(cur_dir, tokens.get(1));
            File des = new_file(cur_dir, tokens.get(2));

            // 执行操作的文件不存在
            if (!src.exists() || !des.exists()) {
                System.out.println("想执行操作的文件或文件夹不存在。");
            }

            // 对文件执行操作
            else if (src.isFile()) {
                try {
                    File des_file = new_file(des, src.getName());
                    copyFile(src, des_file);
                    System.out.println("文件拷贝操作成功。");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 对文件夹操作
            else if(src.isDirectory()){
                    // 如果源文件（夹）是目的地文件夹本身或其任意层级的祖先，则报告给用户逻辑错误。
                    if (src.getCanonicalPath().equals(des.getCanonicalPath()) || is_Ancestor(des, src)) {
                        System.out.println("源文件（夹）是目的地文件夹本身或其任意层级的祖先， 逻辑错误。");
                        throw new IOException("非法的复制：源文件夹是目标的祖先或自身。");
                    }

                    // 如果在目的地文件夹中，源文件（夹）同名的文件（夹）已存在，则也需要报告给用户。
                    if (file_exists(src, des)) {
                        System.out.println("源文件（夹）同名的文件（夹）已存在。");
                    }

                    copyDirectory(src, des);
                    System.out.println("文件夹拷贝操作成功。");
            }
        }
    }

    private void handleCopy_2(ArrayList<String> tokens, File cur_dir, File f_2) throws IOException {
        // 显示用法
        if (tokens.size() == 1) {
            System.out.println("命令copy实现文件和文件夹的拷贝，是单参或双参的命令。");
        }

        // 不符合参数条件
        else if (tokens.size() > 3) {
            System.out.println("命令copy实现文件和文件夹的拷贝，是单参或双参的命令。");
        }

        // 单参
        else if (tokens.size() == 2) {
            // 相对路径 绝对路径都可以
            File cur_file = new_file(cur_dir, tokens.get(1));

            // 文件不存在
            if (!cur_file.exists()) {
                System.out.println("该文件或文件夹不存在。");
                throw new IOException("该文件或文件夹不存在");
            }

            // 命名冲突
            if (file_exists(cur_file, cur_dir)) {
                System.out.println("想要拷贝的文件或文件夹在当前工作目录中已经存在。");
                throw new IOException("想要拷贝的文件或文件夹在当前工作目录中已经存在");
            }

            // 逻辑爆炸
            else if (this.path_explode(cur_file)) {
                System.out.println("拷贝逻辑爆炸。");
                throw new IOException("拷贝逻辑爆炸。");
            }

            // 开始复制
            else {
                if (cur_file.isFile()) {
                    try {
                        copyFile(cur_file, new_file(cur_dir, cur_file.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("文件拷贝成功。");
                } else if (cur_file.isDirectory()) {
                    try {
                        copyDirectory(cur_file, f_2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("文件拷贝成功。");
                }
            }
        }

        // 双参
        else {
            File src = new_file(cur_dir, tokens.get(1));
            File des = new_file(cur_dir, tokens.get(2));

            // 执行操作的文件不存在
            if (!src.exists() || !des.exists()) {
                System.out.println("想执行操作的文件或文件夹不存在。");
            }

            // 对文件执行操作
            else if (src.isFile()) {
                try {
                    File des_file = new_file(des, src.getName());
                    copyFile(src, des_file);
                    System.out.println("文件拷贝操作成功。");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 对文件夹操作
            else if(src.isDirectory()){
                // 如果源文件（夹）是目的地文件夹本身或其任意层级的祖先，则报告给用户逻辑错误。
                if (src.getCanonicalPath().equals(des.getCanonicalPath()) || is_Ancestor(des, src)) {
                    System.out.println("源文件（夹）是目的地文件夹本身或其任意层级的祖先， 逻辑错误。");
                    throw new IOException("非法的复制：源文件夹是目标的祖先或自身。");
                }

                // 如果在目的地文件夹中，源文件（夹）同名的文件（夹）已存在，则也需要报告给用户。
                if (file_exists(src, des)) {
                    System.out.println("源文件（夹）同名的文件（夹）已存在。");
                }

                copyDirectory(src, f_2);
                System.out.println("文件夹拷贝操作成功。");
            }
        }
    }

    private void handleMove(ArrayList<String> tokens) throws IOException {
        if(tokens.size() == 1) {
            System.out.println("命令move用于把一个文件（夹）移动至目的地文件夹。它是单参和双参命令。");
        }
        else if(tokens.size() > 3) {
            System.out.println("命令move是单参和双参指令。");
        }

        // 单参 向当前目录移动
        else if(tokens.size() == 2) {
            try {
                File f = new_file(this.currentDir, tokens.get(1));
                // 拷贝文件
                if(f.isFile()) {
                    this.handleCopy(tokens, this.currentDir);
                }

                // 拷贝文件夹
                else if(f.isDirectory()) {
                    File f_2 = new_file(this.currentDir, f.getName());
                    this.handleCopy_2(tokens, this.currentDir, f_2);
                }
            } catch (IOException e) {
                return; // 出现异常，终止后续执行
            }
            this.forceDel(tokens, this.currentDir);
        }

        // 双参 src -> des
        else {
            try {
                File f = new_file(this.currentDir, tokens.get(1));
                // 拷贝文件
                if(f.isFile()) {
                    this.handleCopy(tokens, this.currentDir);
                }

                // 拷贝文件夹
                else if(f.isDirectory()) {
                    File f_2 = new_file(new_file(this.currentDir, tokens.get(2)), f.getName());
                    this.handleCopy_2(tokens, this.currentDir, f_2);
                }
            } catch (IOException e) {
                return; // 出现异常，终止后续执行
            }

            ArrayList<String> tokens_1 = new ArrayList<>();
            tokens_1.add(tokens.get(1));
            this.forceDel(tokens_1, this.currentDir);
        }
    }

    private void forceDel(ArrayList<String> tokens, File cur_dir) throws IOException {
        // 直接执行删除操作
        int index = (tokens.size() > 1) ? 1 : 0;
        File cur_file = new_file(cur_dir, tokens.get(index));

        // 文件不存在
        if(!cur_file.exists()) {
            System.out.println("文件不存在。");
        }

        // 文件存在
        else {
            this.delete_file_Or_dir(cur_file);
            if (cur_file.exists()) {
                System.out.println("删除失败。");
            }
            else {
                System.out.println("删除成功。");
            }
        }
    }

    private void handleDel(ArrayList<String> tokens, Scanner scanner, File cur_dir) throws IOException {
        if (tokens.size() == 1) {
            System.out.println("命令del用于级联删除一个文件或文件夹，是一个单参命令。");
        } else if (tokens.size() > 2) {
            System.out.println("del命令是一个单参命令，请重新输入。");
        } else {
            File cur_file = new_file(cur_dir, tokens.get(1));
            // 根路径不能删除
            if (cur_file.getCanonicalPath().equals("/")) {
                System.out.println("不能删除根路径。");
            }

            // 当前目录和当前目录的任何祖先文件夹都不能删除
            else if (cur_file.getCanonicalPath().equals(cur_dir.getCanonicalPath()) || is_Ancestor(cur_dir, cur_file)) {
                System.out.println("不能删除当前工作目录或其任意层级的祖先。");
            }

            // 循环提问Y/N
            else {
                while (true) {
                    System.out.println("将要删除文件或文件夹，及文件夹下面的所有内容，确定吗?(Y/N)");
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) continue;

                    // 处理Y操作
                    if (input.toLowerCase().equals("y")) {
                        // 执行删除操作

                        // 文件不存在
                        if (!cur_file.exists()) {
                            System.out.println("所给参数表示的文件（夹）不存在。");
                            break;
                        }

                        // 文件存在
                        else {
                            this.delete_file_Or_dir(cur_file);
                            if (cur_file.exists()) {
                                System.out.println("删除失败。");
                                break;
                            } else {
                                System.out.println("删除成功。");
                                break;
                            }
                        }
                    }

                    // 处理N操作
                    else if (input.toLowerCase().equals("n")) {
                        // 跳出指令，准备接受下一条用户指令
                        break;
                    }

                    // 处理其他非法操作
                    else {
                        continue;
                    }
                }
            }
        }
    }

    // 工具方法

    // 判断输入路径
    private File new_file(File prefix, String file_name) throws IOException {
        File path = new File(file_name);

        if (path.isAbsolute()) {
            // 绝对路径，不做处理
            return path;

        } else {
            // 相对路径，加上前缀目录
            return new File(prefix.getCanonicalPath() + "/" + file_name);
        }
    }

    private String formatFileSize(long size) {
        if (size >= 1024L * 1024 * 1024)
            return String.format("%.2fG", size / (1024.0 * 1024 * 1024));
        else if (size >= 1024L * 1024)
            return String.format("%.2fM", size / (1024.0 * 1024));
        else if (size >= 1024)
            return String.format("%.2fK", size / 1024.0);
        else
            return size + "B";
    }

    private void delete_file_Or_dir(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                file.delete(); // 可以直接删除空文件夹
            } else {
                for (File child : children) {
                    delete_file_Or_dir(child);
                }
            }
        }

        // 如果是文件夹就直接删除
        file.delete();
    }

    private void calculate_dir(ArrayList<ArrayList<Object>> line, File cur_file) {
        if (cur_file == null) return;

        File[] files = cur_file.listFiles();
        if (files == null) return;  // 目录为空或无权限访问

        for (File file : files) {
            ArrayList<Object> row = new ArrayList<>();

            // 文件名
            row.add(file.getName());

            // 是否为文件夹
            row.add(file.isDirectory() ? "文件夹" : "文件");

            // 计算最大深度（仅对文件夹有意义）
            row.add(getMaxDepth(file, 1));

            // 子文件夹数量
            row.add(getDirNum(file));

            // 所有文件数量
            row.add(getFileNum(file));

            // 所有文件大小总和
            row.add(formatFileSize(getAllFileSize(file)));

            // 加入到主表
            line.add(row);
        }
    }

    private void calculate_file(ArrayList<ArrayList<Object>> line, File cur_file) {
        if (cur_file == null) return;

        ArrayList<Object> row = new ArrayList<>();

        // 文件名
        row.add(cur_file.getName());

        // 是否为文件夹
        row.add(cur_file.isDirectory() ? "文件夹" : "文件");

        // 计算最大深度（仅对文件夹有意义）
        row.add(getMaxDepth(cur_file, 1));

        // 子文件夹数量
        row.add(getDirNum(cur_file));

        // 所有文件数量
        row.add(getFileNum(cur_file));

        // 所有文件大小总和
        row.add(formatFileSize(getAllFileSize(cur_file)));

        // 加入到主表
        line.add(row);
    }

    // 表示des中有与src相同的文件
    private boolean file_exists(File src, File des) {
        File[] children = des.listFiles();
        if (children == null) {
            return false;
        }

        for (File child : children) {
            if (child.getName().equals(src.getName())) {
                return true;
            }
        }

        return false;
    }

    private int getMaxDepth(File file, int currentDepth) {
        if (!file.isDirectory()) {
            return currentDepth;
        }

        File[] sub_files = file.listFiles();
        if (sub_files == null || sub_files.length == 0) {
            return currentDepth;
        }

        int max = currentDepth;
        for (File sub_file : sub_files) {
            if (file.isDirectory()) {
                max = Math.max(max, getMaxDepth(sub_file, currentDepth + 1));
            }
        }

        return max;
    }

    private int getDirNum(File file) {
        if (file.isFile()) {
            return 0;
        }

        File[] sub_files = file.listFiles();
        if (sub_files == null || sub_files.length == 0) {
            return 0;
        }

        int res = 0;
        for (File sub_file : sub_files) {
            if (sub_file.isDirectory()) {
                res += 1;
                res += getDirNum(sub_file);
            }
        }

        return res;
    }

    private int getFileNum(File file) {
        if (file.isFile()) {
            return 1;
        }

        File[] sub_files = file.listFiles();
        if (sub_files == null || sub_files.length == 0) {
            return 0;
        }

        int res = 0;
        for (File sub_file : sub_files) {
            res += getFileNum(sub_file);
        }

        return res;
    }

    private long getAllFileSize(File file) {
        if (!file.isDirectory()) {
            return file.length();
        }

        File[] sub_files = file.listFiles();
        if (sub_files == null || sub_files.length == 0) {
            return 0;
        }

        long res = 0;
        for (File sub_file : sub_files) {
            if (!sub_file.isDirectory()) {
                res += getAllFileSize(sub_file);
            }
        }

        return res;
    }

    // 拷贝逻辑爆炸：单参表示一个文件夹，且这个文件夹是当前工作目录自身或其任意层级的祖先
    private boolean path_explode(File cur_file) throws IOException {
        if (cur_file.isDirectory()) {
            if (cur_file.getCanonicalPath().equals(this.currentDir.getCanonicalPath()) || is_Ancestor(this.currentDir, cur_file))
                return true;
        }

        return false;
    }

    private boolean is_Ancestor(File src_file, File tar_file) {
        if (src_file == null || tar_file == null) {
            return false;
        }

        try {
            String tarPath = tar_file.getCanonicalPath();

            File current = src_file;

            // 一直往上遍历，检查是否遇到 tar_file
            while (current != null) {
                if (current.getCanonicalPath().equals(tarPath)) {
                    return true;
                }
                current = current.getParentFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void copyFile(File src, File des) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(des);
        ) {
            byte[] buffer = new byte[1024]; // 1KB
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private void copyDirectory(File src, File des) throws IOException {
        // 如果目标目录不存在，则创建
        if (!des.exists()) {
            boolean created = des.mkdirs();
            if (!created) {
                throw new IOException("无法创建目标目录: " + des.getCanonicalPath());
            }
        }

        // 获取子文件和子文件夹
        File[] files = src.listFiles();
        if (files == null) return;

        for (File file : files) {
            File destFile = new_file(des, file.getName());

            if (file.isDirectory()) {
                // 递归复制子目录
                copyDirectory(file, destFile);
            } else if (file.isFile()) {
                // 复制文件
                copyFile(file, destFile);
            }
        }
    }
}
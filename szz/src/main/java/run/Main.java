package run;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.*;


/**
 * Copyright 2018 Filipe Falcão Batista dos Santos
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Main {

    @Parameter(names = "--input", description = "The path to the input bug reports directory", order = 1)
    private String input = "";

    @Parameter(names = "--output", description = "The path to the output bug-introducing changes directory", order = 2)
    private String output = "";

    @Parameter(names = "--repository", description = "The path to the repository", order = 3)
    private String repository = "";

    @Parameter(names = "--url", description = "The url of the Git repository to clone, i.e., " +
            "https://github.com/square/okhttp.git")
    private String url = "";

    @Parameter(names = "--diffj", description = "The path to the DiffJ jar executable", order = 4)
    private String diffj = "diffj-master-1.6.3.jar";

    @Parameter(names = "--help", description = "Show the usage of the program", help = true, order = 5)
    private boolean help = false;

    /**
     * The szz main method.
     *
     * @param args the command line args used as input to JCommander
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {

        // Create a JCommander instance and start the application
        Main main = new Main();
        JCommander jCommander = new JCommander(main, args);

        // Init the algorithm
        main.run(jCommander);

    }

    private void run(JCommander jCommander) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {

        // Create and run the Run class
        Run szzRun = new Run(this.input, this.output, this.repository, this.url, this.diffj, this.help);
        szzRun.run(jCommander);

    }

}

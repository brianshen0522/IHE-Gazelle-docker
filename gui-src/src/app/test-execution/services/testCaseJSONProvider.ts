import { promises as fs } from "node:fs";
import path from "node:path";

export const readMlangFileFromTestCaseName = async (testCaseName: string): Promise<string> => {
  const folderPath = process.env.GZL_TEST_CASE_FOLDER;
  if (!folderPath) {
    throw new Error("GZL_TEST_CASE_FOLDER environment variable is not set");
  }

  try {
    const filesInDir = await fs.readdir(folderPath);
    const mlangFiles = filesInDir.filter((file) => path.extname(file) === ".mlang");

    for (const file of mlangFiles) {
      const filePath = path.join(folderPath, file);
      const content = await fs.readFile(filePath, "utf8");

      // Check that file contains "Test ${testCaseName}"
      if (content.includes(`Test ${testCaseName}`)) {
        return content;
      }
    }

    throw new Error(`Aucun fichier MLANG contenant le test "${testCaseName}" n'a été trouvé`);
  } catch (error) {
    console.error(`Erreur lors de la lecture du fichier MLANG pour le test ${testCaseName}:`, error);
    throw error;
  }
};

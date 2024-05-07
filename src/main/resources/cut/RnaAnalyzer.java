/**
 * This component helps detecting viruses and their mutations based
 * on RNA (Ribonucleic acid) data.
 * The data are strings over the four-letter alphabet {A, C, G, U}.
 */
public class RnaAnalyzer {

    String virusRnaSequence;

    public RnaAnalyzer(String virusRnaSequence) {
        this.virusRnaSequence = stripNonRnaChars(virusRnaSequence);
    }

    /**
     * Checks what the longest common subsequence of the virus RNA sequence
     * and the given RNA data is.
     *
     * @param rnaData The RNA data to compare with the virus RNA sequence.
     *                It may contain non-RNA characters which will be ignored.
     * @return The longest common subsequence of the virus RNA sequence and the given RNA data.
     */
    public String longestCommonRnaSubsequence(String rnaData) {
        String rnaDataCleaned = stripNonRnaChars(rnaData);
        return longestCommonRnaSubsequence(
                virusRnaSequence, rnaDataCleaned,
                virusRnaSequence.length(), rnaDataCleaned.length()
        );
    }

    private String longestCommonRnaSubsequence(String rna1, String rna2, int len1, int len2) {
        System.out.println("Compare " + rna1.substring(0, len1) + "\n  with " + rna2.substring(0, len2));

        if (len1 == 0 || len2 == 0) {
            return "";
        }

        if (rna1.charAt(len1 - 1) == rna2.charAt(len2 - 1)) {
            return longestCommonRnaSubsequence(rna1, rna2, len1 - 1, len2 - 1) + rna1.charAt(len1 - 1);
        } else {
            String s1 = longestCommonRnaSubsequence(rna1, rna2, len1 - 1, len2);
            String s2 = longestCommonRnaSubsequence(rna1, rna2, len1, len2 - 1);
            System.out.println(" -> result: " + s1 + " vs " + s2);
            return s1.length() > s2.length() ? s1 : s2;
        }
    }

    /**
     * Strips all non-RNA characters from the given string.
     * RNA data consists of:
     * A - adenine,
     * C - cytosine,
     * G - guanine,
     * U - uracil.
     */
    private String stripNonRnaChars(String rnaData) {
        return rnaData.toUpperCase().replaceAll("[^ACGU]", "");
    }

}

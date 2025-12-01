public class Sample {
	public static void main(String[] args) {
		// Now switch expressions are final o/

		FeaturePhase switchWithPatternMatching = FeaturePhase.FINAL;

		var password = switch (switchWithPatternMatching) {
			case FINAL -> "No longer need to use `--preview`";
			case PREVIEW -> "Still needs to use `--preview`";
		};

		System.out.println("Switch expressions with pattern matching: " + password);
	}
}

enum FeaturePhase {
	PREVIEW,
	FINAL
}
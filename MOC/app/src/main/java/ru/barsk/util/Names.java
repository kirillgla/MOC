package ru.barsk.util;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.Random;

public final class Names {
	private String[] names = new String[0];
	private final String ru1 = "аааееёииоооуыэюя";
	private final String ru2 = "ббввгджзйккллммннпрстфхцчшщ";
	private final String en1 = "aaaeeeiiouyy";
	private final String en2 = "bbbcdddffgghjkllmmmnnnppqrrssstttvwxz";

	public void giveNewNames(@NonNull String[] newNames) {
		names = newNames;
    }

	public void randomize(String[] names) {
		int i;
		String buffer;
		for (i = 0; i < names.length; i++) {
			int j = (int) (Math.random()*names.length);
			buffer = names[i];
			names[i] = names[j];
			names[j] = buffer;
		}
	}

	public void getNames(String[] linkToArray) {
		for (int i = 0; i < linkToArray.length; i++) {
			if (i < names.length) {
				linkToArray[i] = names[i];
			}
			else {
				linkToArray[i] = makeRandomName();
			}
		}
		randomize(linkToArray);
	}
	
	public String makeRandomName() {
		StringBuilder name = new StringBuilder();
		String locale = Locale.getDefault().getLanguage();
		Random r = new Random();
		String vowels;
		String consonants;
		int index;
		if (locale.equals("ru")) {
			vowels = ru1;
			consonants = ru2;
		}
		else {
			vowels = en1;
			consonants = en2;
		}
		if (r.nextBoolean()) {
			index = (int) (Math.random() * consonants.length());
			name.append(consonants.charAt(index));
		}
		int numberOfSyllables = (int) (Math.random() * 3 + 1);
		for (int i = 0; i < numberOfSyllables; i++) {
			index = (int) (Math.random() * vowels.length());
			name.append(vowels.charAt(index));
			index = (int) (Math.random() * consonants.length());
			name.append(consonants.charAt(index));
		}
		if (r.nextBoolean()) {
			index = (int) (Math.random() * vowels.length());
			name.append(vowels.charAt(index));
		}
		name.setCharAt(0, String.valueOf(name.charAt(0)).toUpperCase().charAt(0));
		return name.toString();
	}

}

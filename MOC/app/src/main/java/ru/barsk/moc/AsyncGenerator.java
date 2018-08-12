package ru.barsk.moc;

import android.os.AsyncTask;

import ru.barsk.util.Lib;
import ru.barsk.util.Names;

/**
 * Создание зозвездий - довольно трудоёмкий процесс,
 * поэтому его следует выделить в отдельный поток.
 * Данный класс реализует этот поток.
 */
public class AsyncGenerator extends AsyncTask<String[], Void, Object[]> {

	private byte size;
	private byte density;
	private byte numberOfPlayers;

	public AsyncGenerator(byte size, byte density, byte numberOfPlayers) {
		this.size = size;
		this.density = density;
		this.numberOfPlayers = numberOfPlayers;
	}

	/**
	 * @param params [0] массив названий звёзд
	 * 				 [1] массив назввний ИИ
	 * 				 [2][0] строка-id цвета игрока
	 */
	@Override
	protected Object[] doInBackground(String[]... params) {
		String[] starNames = params[0];
		Names nameManager = new Names();
		nameManager.giveNewNames(starNames);
		Generator generator = new Generator();
		Star[][] galaxy = generator.generateGalaxy(size, density, numberOfPlayers, nameManager);
		AbstractAI[] AIs = new AbstractAI[numberOfPlayers - 1];
		String[] aiNames = params[1];
		nameManager.giveNewNames(aiNames);
		aiNames = new String[numberOfPlayers - 1];
		nameManager.getNames(aiNames);
		Lib.Colors colorManager = new Lib.Colors();
		int playerColor = Integer.parseInt(params[2][0]);
		colorManager.used[playerColor] = true;
		for (byte i = 2; i < numberOfPlayers + 1; i++) {
			byte colorId;
			colorId = colorManager.getFreeColorId();
			AIs[i - 2] = new AI(i, colorId, aiNames[i - 2]);
		}
		return new Object[] {galaxy, AIs};
	}

	@Override
	protected void onPostExecute(Object[] result) {
		super.onPostExecute(result);
		MainActivity.turnLong = 1;
		MainActivity.galaxy = (Star[][]) result[0]; // Прокатит такое кастование или не прокатит?
		MainActivity.AIs = (AbstractAI[]) result[1];
		Sectors.setGalaxy(MainActivity.galaxy);
		MainActivity.context.toSingleplayer();
	}

}

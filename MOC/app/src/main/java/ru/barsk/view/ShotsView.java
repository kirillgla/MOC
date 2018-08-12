package ru.barsk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import ru.barsk.moc.CombatActivity;
import ru.barsk.moc.MainActivity;
import ru.barsk.moc.R;
import ru.barsk.moc.Ship;

/**
 * View, находящаяся в центре экрана CombatActivity.
 * На ней отображаются выстрелы (Shot).
 */
public class ShotsView extends View {

	private static final String LOG_TAG = "ShotsView";
	public int viewWidth;
	public int viewHeight;
	public static final int timerInterval = 50;
	private volatile ArrayList<Shot> shots;
	private Paint paint;
	ShotsViewTimer timer;

	public ShotsView(Context context) {
		super(context);
		shots = new ArrayList<>();
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_base));
		timer = new ShotsViewTimer();
		timer.start();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_base));
		for (Shot shot: shots) {
			shot.draw(canvas, paint);
		}
		paint.setARGB(255, 255, 255, 255);
		for (int i = 0; i < CombatActivity.maxNumberOfShipsOnScreen; i++) {
			Ship ship = CombatActivity.sideOneFightingShips[i];
			String hp;
			if (ship != null) {
				hp = String.valueOf(ship.getHp());
			}
			else {
				hp = "";
			}
			canvas.drawText(String.valueOf(hp), 16, CombatActivity.getY(i), paint);
			ship = CombatActivity.sideTwoFightingShips[i];
			if (ship != null) {
				hp = String.valueOf(ship.getHp());
			}
			else {
				hp = "";
			}
			canvas.drawText(String.valueOf(hp), viewWidth - 16, CombatActivity.getY(i), paint);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		timer.cancel();
		super.finalize();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewHeight = h;
		viewWidth = w;
	}

	public void add(Shot shot) {
		if (shot.damage > 0) {
			this.shots.add(shot);
		}
	}

	/**
	 * @param index index of ship receiving damage
	 * @param left true, if target ship is on left side,
	 *             false otherwise.
	 */
	public int getSumDamage(int index, boolean left) {
		int result = 0;
		for (Shot shot: shots) {
			boolean isDestiningLeft = shot.fromY > viewWidth;
			if (shot.targetIndex == index && isDestiningLeft == left) {
				result += shot.damage;
			}
		}
		return result;
	}

	public class ShotsViewTimer extends CountDownTimer {
		private final String LOG_TAG = "Timer";
		public ShotsViewTimer() {
			super(Integer.MAX_VALUE, timerInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			for (Shot shot: shots) {
				shot.moveToNextPosition(timerInterval);
			}
			int i = 0;
			while (i < shots.size()) {
				Shot shot = shots.get(i);
				if (shot.hasReachedTarget) {
					shot.damage();
					shots.remove(shot);
				}
				else {
					i++;
				}
			}
			invalidate();
		}
		@Override
		public void onFinish() {
			Log.w(LOG_TAG, "Timer finished!");
		}
	}

}

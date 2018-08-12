package ru.barsk.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import ru.barsk.moc.R;

/**
 * Класс, реализующий мигающую иконку
 * на главном экране.
 */
public class AlertView extends View {

	public static final int deltaT = 1000; //ms
	public boolean shining;
	protected Timer timer;

	public void startTimer() {
		timer = new Timer(deltaT);
		timer.start();
	}

	public AlertView(Context context) {
		super(context);
		startTimer();
	}

	public AlertView(Context context, AttributeSet attrs) {
		super(context, attrs);
		startTimer();
	}

	public AlertView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		startTimer();
	}

	@Override
	public void invalidate() {
		// Изменить состояние иконки:
		shining = !shining;
		if (shining) {
			setBackgroundResource(R.drawable.alert_warning);
		}
		else {
			setBackgroundResource(R.drawable.alert_warning2);
		}
		super.invalidate();
	}

	/*@Override
	protected void finalize() throws Throwable {
		timer.stop();
		super.finalize();
	}*/

	/**
	 * Класс-таймер,
	 * через каждые deltaT
	 * изменяет состояние AlertView.
	 */
	public class Timer extends CountDownTimer {
		public Timer(int deltaT) {
			super(Integer.MAX_VALUE, deltaT);
		}
		@Override
		public void onTick(long millis) {
			invalidate();
		}
		@Override
		public void onFinish() {}
		//public void stop() throws Throwable {finalize();}
	}

}

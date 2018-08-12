package ru.barsk.moc;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class Shot {

	private static final String LOG_TAG = "Shot";

	public Ship target;
	public int targetIndex;
	public int damage;
	public int fromX;
	public int fromY;
	public int toX;
	public int toY;
	public int currentX;
	public int currentY;
	public double speedX;
	public double speedY;
	public byte colorId;
	public int timeToReachTarget;
	public long timeOnScreen;
	public boolean hasReachedTarget;

	public Shot(Ship target, int fromX, int fromY, int toX, int toY, byte colorId, int timeToReachTarget, int damage, int targetIndex) {
		this.target = target;
		this.targetIndex = targetIndex;
		this.damage = damage;
		this.toX = toX;
		this.toY = toY;
		this.fromX = fromX;
		this.fromY = fromY;
		this.currentX = fromX;
		this.currentY = fromY;
		this.colorId = colorId;
		//fromX != fromY => deltaX != 0
		int deltaX = toX - fromX;
		int deltaY = toY - fromY;
		this.timeToReachTarget = timeToReachTarget;
		speedX = 1. * deltaX / timeToReachTarget;
		speedY = 1. * deltaY / timeToReachTarget;
		timeOnScreen = 0;
		hasReachedTarget = false;
	}

	public void moveToNextPosition(long ms) {
		timeOnScreen += ms;
		currentX = (int) (fromX + speedX * timeOnScreen);
		currentY = (int) (fromY + speedY * timeOnScreen);
		if (timeOnScreen >= timeToReachTarget) {
			hasReachedTarget = true;
		}
	}

	public void damage() {
		int lowerBorder;
		int higherBorder;
		if (damage <= 1) {
			lowerBorder = 0;
			higherBorder = 2;
		}
		else {
			lowerBorder = damage / 2;
			higherBorder = 3 * damage / 2;
		}
		int actualDamage = (int) (Math.random() * (higherBorder - lowerBorder + 1)) + lowerBorder;
		target.damage(actualDamage);
	}

	public void draw(Canvas canvas, Paint paint) {
		int radius;
		if (damage < 8) {
			radius = 2;
		}
		else {
			if (damage > 40) {
				radius = 10;
			}
			else {
				radius = damage / 4;
			}
		}
		setPaintColor(paint, colorId);
		canvas.drawCircle(currentX, currentY, radius, paint);
		if (Settings.doShowDamage) {
			canvas.drawText(String.valueOf(damage), currentX + 10, currentY + 10, paint);
		}
	}

	public void setPaintColor(Paint paint, byte colorId) {
		switch (colorId) {
			case 1: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_1)); break;
			case 2: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_2)); break;
			case 3: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_3)); break;
			case 4: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_4)); break;
			case 5: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_5)); break;
			case 6: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_6)); break;
			case 7: paint.setColor(ContextCompat.getColor(MainActivity.context, R.color.owner_color_7)); break;
		}
	}

}

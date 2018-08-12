package ru.barsk.moc;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.barsk.util.LongDouble;

public final class Ship {
	
	public static final byte BASE_TINY = 0;
	public static final byte BASE_SMALL = 1;
	public static final byte BASE_MEDIUM = 2;
	public static final byte BASE_LARGE = 3;
	public static final byte BASE_HUGE = 4;
	public static final byte BASE_FREIGHTER = 5;

	public boolean alive;
	public final byte ownerId;
	public final boolean canColonize;
	public String name;
	private final int maxHp;
	private int hp;
	public Coordinates position;
	public Coordinates newPosition;
	public Coordinates destination;
	public boolean hasReachedDestination;
	public boolean isSelectedByPlayer = false;
	public final byte base; //0=tiny,1=small,2=medium,3=large,4=huge,5=freighter
	private final byte attack;
	public final byte speed;
	public final double maintenance;

	public Ship(
			byte ownerId,
			boolean canColonize,
			String name,
			int maxHp,
			int hp,
			byte positionX,
			byte positionY,
			byte destinationX,
			byte destinationY,
			byte base,
			byte attack,
			byte speed,
			double maintenance
	) {
		this.ownerId = ownerId;
		this.canColonize = canColonize;
		this.name = name;
		this.maxHp = maxHp;
		this.hp = hp;
		this.position = new Coordinates(positionX, positionY);
		this.destination = new Coordinates(destinationX, destinationY);
		this.base = base;
		this.attack = attack;
		this.speed = speed;
		this.maintenance = maintenance;
		this.alive = true;
		this.hasReachedDestination = this.position.equals(this.destination);
	}

	public Ship(byte ownerId, String name, byte base, byte attack, byte speed, int maxHp, Coordinates position) {
		this(ownerId, name, base, attack, speed, maxHp, -1, position);
	}

	public Ship(byte ownerId, String name, byte base, byte attack, byte speed, int maxHp, double maintenance, Coordinates position) {
		this.position = position;
		this.destination = position.copy();
		this.hasReachedDestination = true;
		this.alive = true;
		this.ownerId = ownerId;
		this.canColonize = base == BASE_FREIGHTER;
		this.name = name;
		if (maxHp < 1) {
			this.maxHp = 1;
		}
		else {
			this.maxHp = maxHp;
		}
		this.hp = this.maxHp;
		if ((base < 0) || (base > 5)) {
			this.base = 0;
		}
		else {
			this.base = base;
		}
		if (attack <= 0) {
			this.attack = 0;
		}
		else {
			this.attack = attack;
		}
		if (speed <= 0) {
			this.speed = 0;
		}
		else {
			this.speed = speed;
		}
		if (maintenance > 0) {
			this.maintenance = maintenance;
		}
		else {
			if (canColonize) {
				this.maintenance = 0.5;
			}
			else {
				this.maintenance = getMaintenance(this.base);
			}
		}
	}

	public static double getMaintenance(int index) {
		switch (index) {
			case 0:
				return 0.02;
			case 1:
				return 0.15;
			case 2:
				return 2.5;
			case 3:
				return 5;
			case 4:
				return 10;
			case 5:
				return 0.5;
			default:
				return 1e9;
		}
	}

	public int getMaxHp() {
		return (int) (maxHp * LongDouble.getHpModifier(ownerId));
	}
	
	public int getRawMaxHp() {
		return maxHp;
	}

	public int getHp() {
		return (int) (hp * LongDouble.getHpModifier(ownerId));
	}
	
	public int getRawHp() {
		return hp;
	}

	public double danger() {
		return getAttack() * getHp();
	}

	public void setHp(int value) {
		if (value <= 0) {
			hp = 0;
			exterminate();
		}
		else {
			if (value >= getMaxHp()) {
				hp = getMaxHp();
			}
			else {
				hp = (int) (value / LongDouble.getHpModifier(ownerId));
			}
		}
	}

	public void setActualHp(int value) {
		if (value <= 0) {
			hp = 0;
			exterminate();
		}
		else {
			if (value >= maxHp) {
				hp = maxHp;
			}
			else {
				hp = value;
			}
		}
	}

	public void damage(int value) {
		setHp(getHp() - Math.abs(value));
	}

	public void exterminate() {
		this.alive = false;
	}

	public void repair() {
		if (maxHp * 0.1 < 5) {
			if (maxHp * 0.1 > 1) {
				setActualHp((int) Math.round(hp + 0.1 * maxHp));
			}
			else {
				setActualHp(hp + 1);
			}
		}
		else {
			setActualHp(hp + 5);
		}
	}

	/**
	 * Listens group of 4 ships
	 * Should get at least one non-null Ship.
	 * Should be assigned to TextViews representing those ships
	 */
	public static class OnShipClickListener implements View.OnClickListener {
		protected final Ship[] listenedShips = new Ship[4];
		protected final boolean[] shipsAreListened = new boolean[4];
		public OnShipClickListener(Ship arg0, Ship arg1, Ship arg2, Ship arg3) {
			listenedShips[0] = arg0;
			listenedShips[1] = arg1;
			listenedShips[2] = arg2;
			listenedShips[3] = arg3;
			shipsAreListened[0] = listenedShips[0] != null;
			shipsAreListened[1] = listenedShips[1] != null;
			shipsAreListened[2] = listenedShips[2] != null;
			shipsAreListened[3] = listenedShips[3] != null;
		}
		@Override
		public void onClick(View v) {
			//View v should be an instance of TextView!
			//if (!(v instanceof TextView)) {throw new ClassCastException();}
			TextView view = (TextView) v;
			switch (view.getId()) {
				case R.id.adapter_ship_0:
					if (shipsAreListened[0]) {
						listenedShips[0].isSelectedByPlayer = ! listenedShips[0].isSelectedByPlayer;
						if (listenedShips[0].isSelectedByPlayer) {
							view.setText(R.string.selected);
						}
						else {
							view.setText("");
						}
					}
					else {
						Toast.makeText(MainActivity.context, "Alert! Called null ship click!", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.adapter_ship_1:
					if (shipsAreListened[1]) {
						listenedShips[1].isSelectedByPlayer = !listenedShips[1].isSelectedByPlayer;
						if (listenedShips[1].isSelectedByPlayer) {
							view.setText(R.string.selected);
						}
						else {
							view.setText("");
						}
					}
					else {
						Toast.makeText(MainActivity.context, "Alert! Called null ship click!", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.adapter_ship_2:
					if (shipsAreListened[2]) {
						listenedShips[2].isSelectedByPlayer = !listenedShips[2].isSelectedByPlayer;
						if (listenedShips[2].isSelectedByPlayer) {
							view.setText(R.string.selected);
						}
						else {
							view.setText("");
						}
					}
					else {
						Toast.makeText(MainActivity.context, "Alert! Called null ship click!", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.adapter_ship_3:
					if (shipsAreListened[3]) {
						listenedShips[3].isSelectedByPlayer = !listenedShips[3].isSelectedByPlayer;
						if (listenedShips[3].isSelectedByPlayer) {
							view.setText(R.string.selected);
						}
						else {
							view.setText("");
						}
					}
					else {
						Toast.makeText(MainActivity.context, "Alert! Called null ship click!", Toast.LENGTH_SHORT).show();
					}
					break;
			}
			byte x = MainActivity.selectedStarCoordinates.x;
			byte y = MainActivity.selectedStarCoordinates.y;
			Button launch = (Button) MainActivity.context.findViewById(R.id.launch);
			boolean anySelectedShipOnOrbit = MainActivity.galaxy[x][y].anyShipSelected();
			launch.setEnabled(anySelectedShipOnOrbit);
		}
	}

	/**
	 * Listens group of 4 ships
	 * Should get at least one non-null Ship.
	 * Should be assigned to TextViews representing those ships
	 */
	public static class OnShipLongClickListener implements View.OnLongClickListener {
		protected final ArrayList<Ship> nonAdaptedOrbit;
		protected final Ship[] listenedShips = new Ship[4];
		protected final boolean[] shipsAreListened = new boolean[4];
		public OnShipLongClickListener(Ship arg0, Ship arg1, Ship arg2, Ship arg3, ArrayList<Ship> nonAdaptedOrbit) {
			listenedShips[0] = arg0;
			listenedShips[1] = arg1;
			listenedShips[2] = arg2;
			listenedShips[3] = arg3;
			shipsAreListened[0] = listenedShips[0] != null;
			shipsAreListened[1] = listenedShips[1] != null;
			shipsAreListened[2] = listenedShips[2] != null;
			shipsAreListened[3] = listenedShips[3] != null;
			this.nonAdaptedOrbit = nonAdaptedOrbit;
		}
		@Override
		public boolean onLongClick(View v) {
			Intent intent = new Intent(MainActivity.context, ShipViewActivity.class);
			switch (v.getId()) {
				case R.id.adapter_ship_0:
					if (shipsAreListened[0]) {
						Buffer.ship = listenedShips[0];
						Buffer.orbit = nonAdaptedOrbit;
						MainActivity.context.startActivity(intent);
					}
					break;
				case R.id.adapter_ship_1:
					if (shipsAreListened[1]) {
						Buffer.ship = listenedShips[1];
						Buffer.orbit = nonAdaptedOrbit;
						MainActivity.context.startActivity(intent);
					}
					break;
				case R.id.adapter_ship_2:
					if (shipsAreListened[2]) {
						Buffer.ship = listenedShips[2];
						Buffer.orbit = nonAdaptedOrbit;
						MainActivity.context.startActivity(intent);
					}
					break;
				case R.id.adapter_ship_3:
					if (shipsAreListened[3]) {
						Buffer.ship = listenedShips[3];
						Buffer.orbit = nonAdaptedOrbit;
						MainActivity.context.startActivity(intent);
					}
			}
			return false;
		}
	}
	
	public void determineNewPosition() {
		if (MainActivity.galaxy[position.x][position.y].getOwnerId() == this.ownerId) {
			repair();
		}
		if (destination.equals(position)) {
			newPosition = position.copy();
		}
		else {
			int totalDeltaX = destination.x - position.x;
			int totalDeltaY = destination.y - position.y;
			double distance = Math.sqrt(totalDeltaX * totalDeltaX + totalDeltaY * totalDeltaY);
			if (distance <= this.speed + /*->*/Math.sqrt(2) - 1/*<- приемлемая погрешнсть, проявляется при полёте по диагонали со скоростью 1*/) {
				newPosition = destination.copy();
			}
			else {
				if (totalDeltaX == 0) {
					if (totalDeltaY > 0) {
						newPosition = new Coordinates(position.x, (byte) (position.y + speed));
					}
					else {
						newPosition = new Coordinates(position.x, (byte) (position.y - speed));
					}
				}
				else {
					if (totalDeltaY == 0) {
						if (totalDeltaX > 0) {
							newPosition = new Coordinates((byte) (position.x + speed), position.y);
						}
						else {
							newPosition = new Coordinates((byte) (position.x - speed), position.y);
						}
					}
					else {
						int moduleTotalDeltaX = Math.abs(totalDeltaX);
						int moduleTotalDeltaY = Math.abs(totalDeltaY);
						//distance = moduleDistance
						double k = speed / distance;
						double actualModuleDeltaX = k * moduleTotalDeltaX;
						double actualModuleDeltaY = k * moduleTotalDeltaY;
						double actualDeltaX;
						double actualDeltaY;
						//totalDeltaX != 0
						//totalDeltaY != 0
						if (totalDeltaX < 0) {
							actualDeltaX = - actualModuleDeltaX;
						}
						else {
							actualDeltaX = actualModuleDeltaX;
						}
						if (totalDeltaY < 0) {
							actualDeltaY = - actualModuleDeltaY;
						}
						else {
							actualDeltaY = actualModuleDeltaY;
						}
						newPosition = new Coordinates((byte) Math.round(position.x + actualDeltaX), (byte) Math.round(position.y + actualDeltaY));
					}
				}
			}
		}
		if (newPosition.equals(destination) && !hasReachedDestination) {
			hasReachedDestination = true;
			StringBuilder content = new StringBuilder();
			content.append(this.name);
			content.append(" ");
			content.append(MainActivity.context.getString(R.string.has_reached_destination));
			content.append(" (");
			String starName = MainActivity.galaxy[destination.x][destination.y].name;
			if (starName.equals("")) {
				content.append(MainActivity.context.getString(R.string.empty_space));
			}
			else {
				content.append(starName);
			}
			content.append(").");
			String message = content.toString();
			TurnEvent event = new TurnEvent(TurnEvent.Tag.SHIP_REACHED_DESTINATION, MainActivity.galaxy[destination.x][destination.y], message);
			MainActivity.events.add(event);
		}
	}

	public byte getAttack() {
		return (byte) (attack * LongDouble.getAttackModifier(ownerId));
	}
	
	public byte getRawAttack() {
		return attack;
	}

}

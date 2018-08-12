package ru.barsk.moc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public final class ShipListAdapter extends ArrayAdapter<Ship[]> {
	
    private ArrayList<Ship> nonAdaptedArray;
    
    public ShipListAdapter(Context context, Ship[][] adaptedArray, ArrayList<Ship> nonAdaptedArray) {
        super(context, R.layout.item_ship_list_adapter, adaptedArray);
        this.nonAdaptedArray = nonAdaptedArray;
        for (Ship[] ships: adaptedArray) {
            for (Ship ship: ships) {
                if (ship != null) {
                    ship.isSelectedByPlayer = ship.ownerId == Player.ID;
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Получение кораблей
        Ship[] ships = getItem(position);
        boolean[] shipExists =
                {
                        ships[0] != null,
                        ships[1] != null,
                        ships[2] != null,
                        ships[3] != null
                };
        //Создание View
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ship_list_adapter, null);
        }
        //Заполнение View
        Ship.OnShipClickListener listener = new Ship.OnShipClickListener(ships[0], ships[1], ships[2], ships[3]);
        Ship.OnShipLongClickListener longListener = new Ship.OnShipLongClickListener(ships[0], ships[1], ships[2], ships[3], nonAdaptedArray);
        TextView[] shipTextViews = {
                (TextView) convertView.findViewById(R.id.adapter_ship_0),
                (TextView) convertView.findViewById(R.id.adapter_ship_1),
                (TextView) convertView.findViewById(R.id.adapter_ship_2),
                (TextView) convertView.findViewById(R.id.adapter_ship_3)
        };
        if (shipExists[0]) {
            if (ships[0].isSelectedByPlayer) {
                shipTextViews[0].setText(R.string.selected);
                shipTextViews[0].setOnClickListener(listener);
                shipTextViews[0].setOnLongClickListener(longListener);
            }
            else {
                shipTextViews[0].setText("");
            }
			byte colorCode;
			if (ships[0].ownerId == Player.ID) {
				colorCode = Player.colorId;
			}
			else {
				colorCode = MainActivity.AIs[ships[0].ownerId - 2].colorId;
			}
            shipTextViews[0].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId(ships[0].base, colorCode), 0, 0);
        }
        else {
            shipTextViews[0].setText("");
            shipTextViews[0].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId((byte) 0, (byte) 0), 0, 0);
        }
        if (shipExists[1]) {
            if (ships[1].isSelectedByPlayer) {
                shipTextViews[1].setText(R.string.selected);
                shipTextViews[1].setOnClickListener(listener);
                shipTextViews[1].setOnLongClickListener(longListener);
            }
            else {
                shipTextViews[1].setText("");
            }
			byte colorCode;
			if (ships[1].ownerId == Player.ID) {
				colorCode = Player.colorId;
			}
			else {
				colorCode = MainActivity.AIs[ships[1].ownerId - 2].colorId;
			}
            shipTextViews[1].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId(ships[1].base, colorCode), 0, 0);
        }
        else {
            shipTextViews[1].setText("");
            shipTextViews[1].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId((byte) 0, (byte) 0), 0, 0);
        }
        if (shipExists[2]) {
            if (ships[2].isSelectedByPlayer) {
                shipTextViews[2].setText(R.string.selected);
                shipTextViews[2].setOnClickListener(listener);
                shipTextViews[2].setOnLongClickListener(longListener);
            }
            else {
                shipTextViews[2].setText("");
            }
			byte colorCode;
			if (ships[2].ownerId == Player.ID) {
				colorCode = Player.colorId;
			}
			else {
				colorCode = MainActivity.AIs[ships[2].ownerId - 2].colorId;
			}
            shipTextViews[2].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId(ships[2].base, colorCode), 0, 0);
        }
        else {
            shipTextViews[2].setText("");
            shipTextViews[2].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId((byte) 0, (byte) 0), 0, 0);
        }
        if (shipExists[3]) {
            if (ships[3].isSelectedByPlayer) {
                shipTextViews[3].setText(R.string.selected);
                shipTextViews[3].setOnClickListener(listener);
                shipTextViews[3].setOnLongClickListener(longListener);
            }
            else {
                shipTextViews[3].setText("");
            }
			byte colorCode;
			if (ships[3].ownerId == Player.ID) {
				colorCode = Player.colorId;
			}
			else {
				colorCode = MainActivity.AIs[ships[3].ownerId - 2].colorId;
			}
            shipTextViews[3].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId(ships[3].base, colorCode), 0, 0);
        }
        else {
            shipTextViews[3].setText("");
            shipTextViews[3].setCompoundDrawablesWithIntrinsicBounds(0, MainActivity.getShipDrawableId((byte) 0, (byte) 0), 0, 0);
        }
        return convertView;
    }

}

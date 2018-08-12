package ru.barsk.moc;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.widget.*;
import android.view.View.OnClickListener;
import java.util.ArrayList;
import android.view.inputmethod.InputMethodManager;

import ru.barsk.util.Tools;

public final class ShipViewActivity extends Activity {
    
    public Ship ship;
    public ArrayList<Ship> orbit;
    private View layout_main;
    private ImageView shipImageView;
    private TextView nameTextView;
    private TextView hpTextView;
    private TextView attackTextView;
    private TextView speedTextView;
    private TextView canColonizeTextView;
	private TextView maintenanceTextView;
	private TextView positionTextView;
	private TextView destinationTextView;
    private FrameLayout gui_item_1; // 'rename' button or textView
    private FrameLayout gui_item_2; // 'destroy' button or textView
    private FrameLayout gui_item_3; // dialog user interface
    private View gui_item_1_base;
    private View gui_item_1_selected;
    private View gui_item_2_base;
    private View gui_item_2_selected;
    private View gui_item_3_destroy;
    private View gui_item_3_rename;
    private View noItem;
    private Button done;
    
    public int currentMode;
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_RENAME = 1;
    public static final int MODE_DESTROY = 2;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_view);
        initialize();
        currentMode = MODE_DEFAULT;
        invalidate();
        setListeners();
        layout_main.startAnimation(MainActivity.appearance);
    }
    
    public void initialize() {
        layout_main = findViewById(R.id.layout_main);
        shipImageView = (ImageView) findViewById(R.id.shipImageView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        hpTextView = (TextView) findViewById(R.id.hpTextView);
        attackTextView = (TextView) findViewById(R.id.attackTextView);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        canColonizeTextView = (TextView) findViewById(R.id.canColonizeTextView);
		maintenanceTextView = (TextView) findViewById(R.id.maintenanceTextView);
		positionTextView = (TextView) findViewById(R.id.positionTextView);
		destinationTextView = (TextView) findViewById(R.id.destinationTextView);
        gui_item_1 = (FrameLayout) findViewById(R.id.gui_item_1);
        gui_item_2 = (FrameLayout) findViewById(R.id.gui_item_2);
        gui_item_3 = (FrameLayout) findViewById(R.id.gui_item_3);
        done = (Button) findViewById(R.id.done);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.context);
        gui_item_3_destroy = inflater.inflate(R.layout.interface_secondary_destroy, null);
        gui_item_3_rename = inflater.inflate(R.layout.interface_secondary_rename, null);
        gui_item_1_base = inflater.inflate(R.layout.item_rename_base, null);
        gui_item_1_selected = inflater.inflate(R.layout.item_selected, null);
        ((TextView) gui_item_1_selected).setText(R.string.rename2);
        gui_item_2_base = inflater.inflate(R.layout.item_destroy_base, null);
        gui_item_2_selected = inflater.inflate(R.layout.item_selected, null);
        ((TextView) gui_item_2_selected).setText(R.string.destroy2);
        noItem = inflater.inflate(R.layout.item_null, null);
        ship = Buffer.ship;
        orbit = Buffer.orbit;
    }
    
    public void invalidate() {
        shipImageView.setImageResource(MainActivity.getShipDrawableId(ship.base, Player.colorId));
        nameTextView.setText(ship.name);
        hpTextView.setText(ship.getHp() + "/" + ship.getMaxHp());
        attackTextView.setText("" + ship.getAttack());
        speedTextView.setText("" + ship.speed);
		String position;
		{
			byte x = ship.position.x;
			byte y = ship.position.y;
			position = MainActivity.galaxy[x][y].name;
			if (position.equals("")) {
				position = MainActivity.context.getResources().getString(R.string.empty_space);
			}
		}
		positionTextView.setText(position);
		String destination;
		if (ship.position.equals(ship.destination)) {
			destination = MainActivity.context.getResources().getString(R.string.no);
		}
		else {
			byte x = ship.destination.x;
			byte y = ship.destination.y;
			destination = MainActivity.galaxy[x][y].name;
			if (destination.equals("")) {
				destination = MainActivity.context.getResources().getString(R.string.empty_space);
			}
		}
		destinationTextView.setText(destination);
        if (ship.canColonize) {
            canColonizeTextView.setText(R.string.yes);
        }
        else {
            canColonizeTextView.setText(R.string.no);
        }
		Tools.setColoredValue(Tools.round(-ship.maintenance, (byte) 2), maintenanceTextView, true, true);
        gui_item_1.removeAllViewsInLayout();
        gui_item_2.removeAllViewsInLayout();
        gui_item_3.removeAllViewsInLayout();
        switch (currentMode) {
            case MODE_DEFAULT:
                gui_item_1.addView(gui_item_1_base);
                gui_item_2.addView(gui_item_2_base);
                gui_item_3.addView(noItem);
                break;
            case MODE_RENAME:
                gui_item_1.addView(gui_item_1_selected);
                gui_item_2.addView(gui_item_2_base);
                gui_item_3.addView(gui_item_3_rename);
                break;
            case MODE_DESTROY:
                gui_item_1.addView(gui_item_1_base);
                gui_item_2.addView(gui_item_2_selected);
                gui_item_3.addView(gui_item_3_destroy);
        }
    }

    public void setListeners() {
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        gui_item_1_base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = MODE_RENAME;
                invalidate();
            }
        });
        gui_item_2_base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = MODE_DESTROY;
                invalidate();
            }
        });
        gui_item_3_destroy.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = MODE_DEFAULT;
                invalidate();
            }
        });
        gui_item_3_rename.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = MODE_DEFAULT;
                invalidate();
            }
        });
        gui_item_3_destroy.findViewById(R.id.confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                orbit.remove(ship);
                onBackPressed();
                MainActivity.context.invalidateScreen(true);
            }
        });
        gui_item_3_rename.findViewById(R.id.done).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                EditText nameEditText = (EditText) gui_item_3_rename.findViewById(R.id.nameEditText);
                String newName = nameEditText.getText().toString();
                if (!newName.equals("")) {
                    nameEditText.setText(ship.name);
                    ship.name = newName;
                }
                currentMode = MODE_DEFAULT;
                invalidate();
            }
        });
		((EditText) gui_item_3_rename.findViewById(R.id.nameEditText)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				gui_item_3_rename.findViewById(R.id.done).callOnClick();
				return false;
			}
		});
    }
    
    @Override
    public void onBackPressed() {
        layout_main.startAnimation(MainActivity.disappearance);
        MainActivity.delayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                ShipViewActivity.super.onBackPressed();
            }
        }, 500);
    }
    
}

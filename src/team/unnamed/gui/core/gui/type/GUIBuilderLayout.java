package team.unnamed.gui.core.gui.type;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import team.unnamed.gui.abstraction.item.ItemClickable;
import team.unnamed.gui.abstraction.menu.GUIData;
import team.unnamed.gui.core.gui.factory.GUIFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static team.unnamed.validate.Validate.isNotNull;
import static team.unnamed.validate.Validate.isState;

abstract class GUIBuilderLayout<T extends GUIBuilder> implements GUIBuilder {

	protected final String title;
	protected final int slots;
	private final int rows;

	protected ItemClickable[] items;

	protected Predicate<InventoryOpenEvent> openAction;
	protected Consumer<InventoryCloseEvent> closeAction;

	protected boolean cancelClick = true;

	protected GUIBuilderLayout(String title) {
		this(title, 6);
	}

	protected GUIBuilderLayout(String title, int rows) {
		isNotNull(title, "Title can't be null.");
		isState(rows > 0, "Rows must be major than 0.");

		this.title = title;
		this.slots = rows * 9;
		this.rows = rows;

		items = new ItemClickable[this.slots];
	}

	@Override
	public T fillItem(ItemClickable item, int from, int to) {
		isNotNull(item, "Item clickable can't be null");

		for (int i = from; i < to; i++) {
			items[i] = item.cloneInSlot(i);
		}

		return back();
	}

	@Override
	public T fillRow(ItemClickable item, int row) {
		isState(row > 0 && row <= this.rows, "Row cannot be minor than 0 or major than 6");

		int indexStart = row == 1 ? 0 : (row - 1) * 9;
		int indexEnd = indexStart + 9;

		for (int slot = indexStart; slot < indexEnd; slot++) {
			items[slot] = item.cloneInSlot(slot);
		}

		return back();
	}

	@Override
	public T fillColumn(ItemClickable item, int column) {
		isState(column > 0 && column <= 9, "Column cannot be minor than 0 or major than 9");

		int indexStart = column - 1;
		int indexEnd = (slots - 9) + column;

		for (int slot = indexStart; slot <= indexEnd; slot += 9) {
			items[slot] = item.cloneInSlot(slot);
		}

		return back();
	}

	@Override
	public T fillBorders(ItemClickable item) {
		if (rows < 3) {
			return back();
		}

		fillRow(item, 1);
		fillRow(item, rows);
		fillColumn(item, 1);
		fillColumn(item, 9);

		return back();
	}

	@Override
	public T setItems(List<ItemClickable> items) {
		isNotNull(items, "Items can't be null.");

		this.items = items.toArray(new ItemClickable[0]);

		return back();
	}

	@Override
	public T addItem(ItemClickable itemClickable) {
		isNotNull(itemClickable, "Item clickable can't be null.");

		items[itemClickable.getSlot()] = itemClickable;

		return back();
	}

	@Override
	public T addItem(ItemClickable itemClickable, int... slots) {
		isNotNull(itemClickable, "Item clickable can't be null.");

		for (int slot : slots) {
			addItem(itemClickable.cloneInSlot(slot));
		}

		return back();
	}

	@Override
	public T openAction(Predicate<InventoryOpenEvent> openAction) {
		this.openAction = openAction;

		return back();
	}

	@Override
	public T closeAction(Consumer<InventoryCloseEvent> closeAction) {
		this.closeAction = closeAction;

		return back();
	}

	@Override
	public T toggleClick() {
		this.cancelClick = !cancelClick;

		return back();
	}

	@Override
	public Inventory build() {
		GUIData guiData = new SimpleGUIData(title, slots, new ArrayList<>(Arrays.asList(items)), openAction,
				closeAction, cancelClick);

		Inventory inventory = GUIFactory.create(guiData);

		if (inventory == null) {
			return null;
		}

		for (ItemClickable itemClickable : items) {
			if (itemClickable == null) {
				continue;
			}

			inventory.setItem(itemClickable.getSlot(), itemClickable.getItemStack());
		}

		return inventory;
	}

	protected abstract T back();

}
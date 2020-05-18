package dev.brighten.anticheat.utils.menu.type.impl;

import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.utils.menu.Menu;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.type.BukkitInventoryHolder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class PagedMenu implements Menu {
    @Getter
    @Setter
    String title;
    final MenuDimension dimension;
    @Setter
    private Menu parent;
    @Getter
    @Setter
    private int currentPage;
    @Getter
    BukkitInventoryHolder holder;
    public List<Button> contents;
    private CloseHandler closeHandler;
    public PagedMenu(@NonNull String title, int size) {
        this.title = title.length() > 32 ? title.substring(0, 32) : title;
        if (size <= 0 || size > 6) {
            throw new IndexOutOfBoundsException("A menu can only have between 1 & 6 for a size (rows)");
        }
        this.dimension = new MenuDimension(size, 9);
        this.contents = new ArrayList<>();
    }

    @Override
    public MenuDimension getMenuDimension() {
        return dimension;
    }

    @Override
    public void addItem(Button button) {
        setItem(getFirstEmptySlot(), button);
    }

    @Override
    public void setItem(int index, Button button) {
        checkBounds(index);
        contents.add(index, button);
    }

    @Override
    public void fill(Button button) {
        fillRange(0, dimension.getSize(), button);
    }

    @Override
    public void fillRange(int startingIndex, int endingIndex, Button button) {
        IntStream.range(startingIndex, endingIndex)
                .filter(i -> contents.get(i) == null || contents.get(i).getStack().getType()
                        .equals(XMaterial.AIR.parseMaterial()))
                .forEach(i -> setItem(i, button));
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < contents.size(); i++) {
            Button button = contents.get(i);
            if (button == null) {
                return i;
            }
        }
        return -1; // Will throw when #checkBounds is called.
    }

    @Override
    public void checkBounds(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > (dimension.getSize())) {
            throw new IndexOutOfBoundsException(String.format("setItem(); %s is out of bounds!", index));
        }
    }

    @Override
    public Optional<Button> getButtonByIndex(int index) {
        if(index >= contents.size() - 1) return Optional.empty();

        return Optional.ofNullable(contents.get(index));
    }

    @Override
    public void buildInventory(boolean initial) {
        if (initial) {
            this.holder = new BukkitInventoryHolder(this);
            holder.setInventory(Bukkit.createInventory(holder, dimension.getSize(), title));
        }
        holder.getInventory().clear();
        int size = (dimension.getRows() - 1) * dimension.getColumns();
        IntStream.range(Math.min(contents.size(), size * (currentPage - 1)),
                Math.min(contents.size(), size * currentPage))
                .forEach(i -> {
                    Button button = contents.get(i);
                    if (button != null) {
                        holder.getInventory().setItem(i, button.getStack());
                    }
                });
    }

    @Override
    public void showMenu(Player player) {
        if (holder == null) {
            buildInventory(true);
        } else {
            buildInventory(false);
        }
        player.openInventory(holder.getInventory());
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
        handleClose(player);
    }

    @Override
    public void setCloseHandler(CloseHandler handler) {
        this.closeHandler = handler;
    }

    @Override
    public void handleClose(Player player) {
        if (closeHandler != null) {
            closeHandler.accept(player, this);
        }
    }

    @Override
    public Optional<Menu> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Iterator<Button> iterator() {
        return contents.iterator();
    }
}

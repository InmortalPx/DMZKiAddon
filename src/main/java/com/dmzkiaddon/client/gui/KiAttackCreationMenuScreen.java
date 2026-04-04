package com.dmzkiaddon.client.gui;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.CreateCustomAttackC2S;
import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dragonminez.Reference;
import com.dragonminez.client.gui.buttons.ColorSlider;
import com.dragonminez.client.gui.buttons.CustomTextureButton;
import com.dragonminez.client.gui.character.BaseMenuScreen;
import com.dragonminez.common.init.MainSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class KiAttackCreationMenuScreen extends BaseMenuScreen {

	private static final ResourceLocation MENU_BG = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID,
			"textures/gui/menu/menubig.png");
	private static final ResourceLocation STAT_BUTTONS = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID,
			"textures/gui/buttons/characterbuttons.png");

	private AttackType currentType = AttackType.KAMEHAMEHA;
	private EditBox nameBox;
	private ColorSlider sliderHue;
	private ColorSlider sliderSaturation;
	private ColorSlider sliderBrightness;
	private float powerMultiplier = 1.0f;
	private boolean shouldClose = false;

	private static final int PANEL_W = 141;
	private static final int PANEL_H = 213;

	public KiAttackCreationMenuScreen(Component title) {
		super(title);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = getUiWidth() / 2;
		int centerY = getUiHeight() / 2;
		int panelX = centerX - PANEL_W / 2;
		int panelY = centerY - PANEL_H / 2;

		int contentX = panelX + 17;
		int contentW = PANEL_W - 34;
		int y = panelY + 35;

		this.nameBox = new EditBox(this.font, contentX, y, contentW, 14, Component.literal("Name"));
		this.nameBox.setMaxLength(32);
		this.nameBox.setValue("Custom Attack");
		this.addRenderableWidget(this.nameBox);
		y += 22;

		this.addRenderableWidget(new CustomTextureButton.Builder()
				.position(contentX, y)
				.size(14, 11)
				.texture(STAT_BUTTONS)
				.textureCoords(142, 0, 142, 10)
				.textureSize(10, 10)
				.onPress(btn -> cycleType(-1))
				.build());

		this.addRenderableWidget(new CustomTextureButton.Builder()
				.position(contentX + contentW - 14, y)
				.size(14, 11)
				.texture(STAT_BUTTONS)
				.textureCoords(0, 0, 0, 10)
				.textureSize(10, 10)
				.onPress(btn -> cycleType(1))
				.build());
		y += 20;

		this.addRenderableWidget(new CustomTextureButton.Builder()
				.position(contentX, y)
				.size(14, 11)
				.texture(STAT_BUTTONS)
				.textureCoords(142, 0, 142, 10)
				.textureSize(10, 10)
				.onPress(btn -> {
					powerMultiplier = Math.max(0.1f, powerMultiplier - 0.1f);
					powerMultiplier = Math.round(powerMultiplier * 10f) / 10f;
				})
				.build());

		this.addRenderableWidget(new CustomTextureButton.Builder()
				.position(contentX + contentW - 14, y)
				.size(14, 11)
				.texture(STAT_BUTTONS)
				.textureCoords(0, 0, 0, 10)
				.textureSize(10, 10)
				.onPress(btn -> {
					powerMultiplier = Math.min(3.0f, powerMultiplier + 0.1f);
					powerMultiplier = Math.round(powerMultiplier * 10f) / 10f;
				})
				.build());
		y += 22;

		sliderHue = new ColorSlider(contentX, y, contentW, 12, 0, 360, 0,
				Component.literal("Hue"), val -> {
					float h = (float) val;
					if (sliderSaturation != null)
						sliderSaturation.setCurrentHue(h);
					if (sliderBrightness != null)
						sliderBrightness.setCurrentHue(h);
				});
		this.addRenderableWidget(sliderHue);
		y += 16;

		sliderSaturation = new ColorSlider(contentX, y, contentW, 12, 100, 0, 100,
				Component.literal("Saturation"), val -> {
					if (sliderBrightness != null)
						sliderBrightness.setCurrentSaturation((float) val);
				});
		sliderSaturation.setCurrentHue(0f);
		sliderSaturation.setCurrentSaturation(100f);
		this.addRenderableWidget(sliderSaturation);
		y += 16;

		sliderBrightness = new ColorSlider(contentX, y, contentW, 12, 100, 0, 100,
				Component.literal("Value"), val -> {
				});
		sliderBrightness.setCurrentHue(0f);
		sliderBrightness.setCurrentSaturation(100f);
		this.addRenderableWidget(sliderBrightness);
		y += 20;

		this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
				Component.literal("Create"), btn -> {
					float hue = sliderHue.getValue() / 360f;
					float sat = sliderSaturation.getValue() / 100f;
					float bri = sliderBrightness.getValue() / 100f;
					int rgb = Color.HSBtoRGB(hue, sat, bri);
					float r = ((rgb >> 16) & 0xFF) / 255f;
					float g = ((rgb >> 8) & 0xFF) / 255f;
					float b = (rgb & 0xFF) / 255f;

					AddonNetworkHandler.sendToServer(new CreateCustomAttackC2S(
							currentType, nameBox.getValue(),
							r, g, b, powerMultiplier));

					if (this.minecraft != null && this.minecraft.player != null) {
						this.minecraft.player.playSound(MainSounds.UI_MENU_SWITCH.get(), 1.0f, 1.0f);
					}
					shouldClose = true;
				}).bounds(contentX + 10, y, contentW - 20, 16).build());
	}

	@Override
	public void tick() {
		super.tick();
		if (shouldClose && this.minecraft != null) {
			this.minecraft.setScreen(null);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		int uiMouseX = (int) Math.round(toUiX(mouseX));
		int uiMouseY = (int) Math.round(toUiY(mouseY));

		beginUiScale(graphics);

		int centerX = getUiWidth() / 2;
		int centerY = getUiHeight() / 2;
		int panelX = centerX - PANEL_W / 2;
		int panelY = centerY - PANEL_H / 2;

		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		graphics.blit(MENU_BG, panelX, panelY, 0, 0, PANEL_W, PANEL_H, 256, 256);

		drawCenteredStringWithBorder(graphics,
				Component.literal("Create Ki Attack").withStyle(ChatFormatting.BOLD),
				centerX, panelY + 17, 0xFFFFD700);

		int contentX = panelX + 17;
		int contentW = PANEL_W - 34;
		int y = panelY + 35;

		y += 22;

		drawCenteredStringWithBorder(graphics,
				Component.literal(currentType.name()),
				contentX + contentW / 2, y + 2, 0xFFFFFFFF);
		y += 20;

		drawCenteredStringWithBorder(graphics,
				Component.literal(String.format("Power: %.1fx", powerMultiplier)),
				contentX + contentW / 2, y + 2, 0xFFFFFFFF);
		y += 22;

		int previewW = 14;
		int previewH = 48;
		int previewX = panelX + PANEL_W + 4;
		int previewY = y - 2;

		float hue = sliderHue != null ? sliderHue.getValue() / 360f : 0f;
		float sat = sliderSaturation != null ? sliderSaturation.getValue() / 100f : 1f;
		float bri = sliderBrightness != null ? sliderBrightness.getValue() / 100f : 1f;
		int previewColor = Color.HSBtoRGB(hue, sat, bri);
		int displayColor = 0xFF000000 | (previewColor & 0x00FFFFFF);

		graphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, displayColor);

		graphics.fill(previewX - 1, previewY - 1, previewX + previewW + 1, previewY, 0xFF333333);
		graphics.fill(previewX - 1, previewY + previewH, previewX + previewW + 1, previewY + previewH + 1, 0xFF333333);
		graphics.fill(previewX - 1, previewY - 1, previewX, previewY + previewH + 1, 0xFF333333);
		graphics.fill(previewX + previewW, previewY - 1, previewX + previewW + 1, previewY + previewH + 1, 0xFF333333);

		super.render(graphics, uiMouseX, uiMouseY, partialTick);
		endUiScale(graphics);
	}

	private void cycleType(int direction) {
		AttackType[] types = AttackType.values();
		int nextIdx = (currentType.ordinal() + direction + types.length) % types.length;
		currentType = types[nextIdx];
	}

	private void drawCenteredStringWithBorder(GuiGraphics graphics, Component text, int centerX, int y, int textColor) {
		int textWidth = this.font.width(text);
		int x = centerX - (textWidth / 2);
		drawStringWithBorder(graphics, text, x, y, textColor);
	}

	private void drawStringWithBorder(GuiGraphics graphics, Component text, int x, int y, int textColor) {
		int borderColor = 0xFF000000;
		graphics.drawString(this.font, text, x + 1, y, borderColor, false);
		graphics.drawString(this.font, text, x - 1, y, borderColor, false);
		graphics.drawString(this.font, text, x, y + 1, borderColor, false);
		graphics.drawString(this.font, text, x, y - 1, borderColor, false);
		graphics.drawString(this.font, text, x, y, textColor, false);
	}
}

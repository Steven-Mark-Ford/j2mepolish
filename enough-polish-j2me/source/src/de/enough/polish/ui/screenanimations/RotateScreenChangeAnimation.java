//#condition polish.usePolishGui && polish.midp2 && polish.hasFloatingPoint
/*
 * Created on Mar 30, 2008 at 5:09:24 PM.
 * 
 * Copyright (c) 2010 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package de.enough.polish.ui.screenanimations;

import de.enough.polish.ui.Display;
import de.enough.polish.ui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.ScreenChangeAnimation;
import de.enough.polish.ui.Style;
import de.enough.polish.util.ImageUtil;

/**
 * <p>Rotates the screen in two dimensions.</p>
 *
 * <p>Copyright Enough Software 2008</p>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class RotateScreenChangeAnimation extends ScreenChangeAnimation
{

	private int[] rotatedScreenRgb;

	/**
	 * Creates a new animation
	 */
	public RotateScreenChangeAnimation()
	{
		super();
	}
	
	

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ScreenChangeAnimation#show(de.enough.polish.ui.Style, javax.microedition.lcdui.Display, int, int, javax.microedition.lcdui.Image, javax.microedition.lcdui.Image, de.enough.polish.ui.Screen)
	 */
	protected void onShow(Style style, Display dsplay, int width, int height,
			Displayable lstDisplayable, Displayable nxtDisplayable, boolean isForward ) 
	{
		if (isForward) {
			this.useLastCanvasRgb = false;
			this.useNextCanvasRgb = true;			
		} else {
			this.useLastCanvasRgb = true;
			this.useNextCanvasRgb = false;
		}
		this.rotatedScreenRgb = new int[ width * height ];
		super.onShow(style, dsplay, width, height, lstDisplayable,
				nxtDisplayable, isForward );
	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ScreenChangeAnimation#animate(long, long)
	 */
	protected boolean animate(long passedTime, long duration) {
		if (passedTime > duration) {
			this.rotatedScreenRgb = null;
			return false;
		}
		int[] source;
		int startDegree, endDegree;
		int startOpacity, endOpacity;
		if (this.isForwardAnimation) {
			source = this.nextCanvasRgb;
			startDegree = 270;
			endDegree = 360;
			startOpacity = 10;
			endOpacity = 255;
		} else {
			source = this.lastCanvasRgb;
			startDegree = 360;
			endDegree = 270;
			startOpacity = 255;
			endOpacity = 10;
		}
		int degrees = calculateAnimationPoint(startDegree, endDegree, passedTime, duration);
		int opacity =  calculateAnimationPoint(startOpacity, endOpacity, passedTime, duration);
		// rotate(int[] sourceRgbData, int width, int height, int referenceX, int referenceY, int backgroundColor, double degreeCos, double degreeSin, int[] rotatedRGB, int rotatedWidth, int rotatedHeight) {
		double degreeCos = Math.cos(Math.PI*degrees/180);
		double degreeSin = Math.sin(Math.PI*degrees/180);
		int backgroundArgb = 0;
		ImageUtil.rotate( source, this.screenWidth, this.screenHeight, this.screenWidth >> 1, this.screenHeight >>1, backgroundArgb, degreeCos, degreeSin,  this.rotatedScreenRgb, this.screenWidth, this.screenHeight, opacity );
		return true;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ScreenChangeAnimation#paintAnimation(javax.microedition.lcdui.Graphics)
	 */
	protected void paintAnimation(Graphics g)
	{
		Image canvasImage;
		if (this.isForwardAnimation) {
			canvasImage = this.lastCanvasImage;
		} else {
			canvasImage = this.nextCanvasImage;
		}
		g.drawImage(canvasImage, 0, 0, Graphics.TOP | Graphics.LEFT);

		g.drawRGB(this.rotatedScreenRgb, 0, this.screenWidth, 0, 0, this.screenWidth, this.screenHeight, true);
	}

}

/* 
 * Copyright (C) 2002-2009 XimpleWare, info@ximpleware.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.ximpleware.xpath;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;

public class VariableExpr extends Expr {
	
	private String exprName;
	private Expr exprVal;
	
	public VariableExpr(String name, Expr e){
		exprName = name;
		exprVal = e;
	}
	
	
	public int adjust(int n) {
		// TODO Auto-generated method stub
		return exprVal.adjust(n);
	}


	public boolean evalBoolean(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalBoolean(vn);
	}


	public int evalNodeSet(VTDNav vn) throws XPathEvalException, NavException {
		// TODO Auto-generated method stub
		return exprVal.evalNodeSet(vn);
	}


	public double evalNumber(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalNumber(vn);
	}


	public String evalString(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalString(vn);
	}


	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return exprVal.isBoolean();
	}


	public boolean isNodeSet() {
		// TODO Auto-generated method stub
		return exprVal.isNodeSet();
	}


	public boolean isNumerical() {
		// TODO Auto-generated method stub
		return exprVal.isNumerical();
	}


	public boolean isString() {
		// TODO Auto-generated method stub
		return exprVal.isString();
	}


	public boolean requireContextSize() {
		// TODO Auto-generated method stub
		return exprVal.requireContextSize();
	}


	public void reset(VTDNav vn) {
		// TODO Auto-generated method stub
		exprVal.reset(vn);
	}


	public void setContextSize(int size) {
		// TODO Auto-generated method stub
		exprVal.setContextSize(size);
	}


	public void setPosition(int pos) {
		// TODO Auto-generated method stub
		exprVal.setPosition(pos);
	}


	public String toString() {
		// TODO Auto-generated method stub
		return "$"+exprName;
	}

}
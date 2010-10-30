/* 
* Copyright (C) 2002-2010 XimpleWare, info@ximpleware.com
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
#include "literalExpr.h"
using namespace com_ximpleware;

LiteralExpr::LiteralExpr(UCSChar *st):
s(st){
}

LiteralExpr::~LiteralExpr(){
	delete s;
	s=NULL;
}

bool LiteralExpr::isNumerical(){return false;}
bool LiteralExpr::isNodeSet(){return false;}
bool LiteralExpr::isString(){return true;}
bool LiteralExpr::isBoolean(){return false;}

void LiteralExpr::reset(VTDNav *vn){}
void LiteralExpr::toString(UCSChar* string){
	wprintf(L"\"");
	wprintf(L"%ls",s);
	wprintf(L"\"");
}

bool LiteralExpr::evalBoolean(VTDNav *vn){
	size_t len = wcslen(s);
	return len != 0;
}

double LiteralExpr::evalNumber(VTDNav *vn){
	double d  = 0, result;
	UCSChar *temp;
	if (wcslen(s)==0)
		return d/d;
	result = wcstod(s,&temp);
	while(*temp!=0){
		if ( *temp == L' '
			|| *temp == L'\n'
			|| *temp == L'\t'
			|| *temp == L'\r'){
				temp++;
			}
		else
			return d/d; //NaN
	}
	return result;
}
int LiteralExpr::evalNodeSet(VTDNav *vn){		
	throw XPathEvalException("LiteralExpr can't eval to a node set!");
}
UCSChar* LiteralExpr::evalString(VTDNav *vn){return wcsdup(s);}
bool LiteralExpr::requireContextSize(){return false;}
void LiteralExpr::setContextSize(int size){}

void LiteralExpr::setPosition(int pos){}
int LiteralExpr::adjust(int n){return 0;}


char* com_ximpleware::getAxisString(axisType at){

	switch(at){
		case AXIS_CHILD : return "child::";
		case AXIS_DESCENDANT : return "descendant::";
		case AXIS_PARENT :	return "parent::";
		case AXIS_ANCESTOR :	return "ancestor::";
		case AXIS_FOLLOWING_SIBLING :	return "following-sibling::";
		case AXIS_PRECEDING_SIBLING :	return "preceding-sibling::";
		case AXIS_FOLLOWING :	return "following::";
		case AXIS_PRECEDING :	return "preceding::";
		case AXIS_ATTRIBUTE :	return "attribute::";
		case AXIS_NAMESPACE :	return "namespace::";
		case AXIS_SELF :	return "self::";
		case AXIS_DESCENDANT_OR_SELF :	return "descendant-or-self::";
		default :	return "ancestor-or-self::";
	}
}


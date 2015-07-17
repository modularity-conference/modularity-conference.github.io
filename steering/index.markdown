---
title: AOSA Steering Committee
menutitle: Steering Committee
short: AOSA
responsible: yes
order: 900
---

The Aspect-Oriented Software Association (AOSA) is a non-profit organization whose
mission is to be the primary sponsor for the annual _Modularity Conference_
(formerly Conference on Aspect-Oriented Software Development/AOSD).

The members of the AOSA Steering Committee are:

<ul>
{% for member in site.data.steering %}
   <li markdown="span">
      [{{ member.name }}]({{member.contact}}){% if member.term %}
       --- Term: {{member.term}}{% endif %}  {% if member.extra %}
       _{{member.extra}}_  --  {% else %}  
       {% endif %}{{ member.affiliation }}
   </li>
{% endfor %}
</ul>

### Former Steering Committee Members

<ul>
{% assign formers =  site.data.former | sort: "lastname" %}
{% for member in formers %}
  <li markdown="span">{{ member.firstname }} {{member.lastname }}{% if member.function %} <small>_({{ member.function }})_</small>{% endif %}</li>
{% endfor %}
</ul>
{% comment %}
  - {{ member.firstname }} {{member.lastname }}{% if member.term %} --- Until {{member.term}}{% endif %}{% if member.function %}  \\
    _({{ member.function }})_{% endif %}
{%endcomment %}


*[AOSA]: Aspect-Oriented Software Association
*[AOSD]: Aspect-Oriented Software Development

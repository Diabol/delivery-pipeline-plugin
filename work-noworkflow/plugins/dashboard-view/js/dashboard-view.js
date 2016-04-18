function getElementFromEvent(e) {
  var targ;
  if (e.target)
    targ = e.target;
  else if (e.srcElement)
    targ = e.srcElement;
  if (targ.nodeType == 3) // defeat Safari bug
    targ = targ.parentNode;
  return targ;
}

function updateIcons(elemName, param)
{
  var cmds = window.document.getElementsByTagName('img');
  var collapse;
  var expand;
  for (var cmdIndex in cmds) {
    cmd = cmds[cmdIndex];
    if (cmd.id && cmd.id == ('cmdExp-'+elemName)) {
      expand = cmd;
    } else if (cmd.id && cmd.id == ('cmdCol-'+elemName)) {
      collapse = cmd;
    }
  }
  expand.style.visibility = (param == 'e') ? 'hidden' : 'visible';
  expand.style.display = (param == 'e') ? 'none' : '';
  collapse.style.visibility = (param == 'c') ? 'hidden' : 'visible';
  collapse.style.display = (param == 'c') ? 'none' : '';
}

function myshow(e) {
  if (!e) var e = window.event;
  var receiver = getElementFromEvent(e);
  var portletId = receiver.id.substring(7);
  var elem = document.getElementById(portletId);
  elem.style.visibility = 'visible';
  elem.style.display = '';

  updateIcons(portletId, 'e');
}

function myhide(e) {
  if (!e) var e = window.event;
  var receiver = getElementFromEvent(e);
  var portletId = receiver.id.substring(7);
  var elem = document.getElementById(portletId);
  elem.style.visibility = 'hidden';
  elem.style.display = 'none';

  updateIcons(portletId, 'c');
}

function startsWith(str, substr) {
  return (str.match("^"+substr) == substr);
}

var cmds = window.document.getElementsByTagName('img');
for (var cmdIndex in cmds) {
  cmd = cmds[cmdIndex];
  if (cmd.id && startsWith(cmd.id, 'cmdExp-dashboard_portlet_')) {
    cmd.onclick = myshow;
//    cmd.style.visibility = 'hidden';
//    cmd.style.display = 'none';
  } else if (cmd.id && startsWith(cmd.id, 'cmdCol-dashboard_portlet_')) {
    cmd.onclick = myhide;
    cmd.style.visibility = 'visible';
    cmd.style.display = '';
  } else if (cmd.id && startsWith(cmd.id, 'cmdMax-dashboard_portlet_')) {
    cmd.style.visibility = 'visible';
    cmd.style.display = '';
  }
}

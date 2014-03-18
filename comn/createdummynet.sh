#!/bin/sh

IMAGE_PATH="/Users/mark/ug3/comn"
VM="DummynetSL6"
HDA_NAME="DummynetSL6"
HDB_NAME="dummynetwork"
MEMORY=512
SHARED_FOLDER_NAME="dummynetshared"
VMM_TOOL="VBoxManage"
SATA_CONTROLLER="\"AHCI\""

if [ ! -n "$IMAGE_PATH" ]; then
    IMAGE_PATH=`dirname $0`
    IMAGE_PATH=${IMAGE_PATH}/images
fi

LOCAL_PATH=`pwd`
HDB_DOWNLOAD_PATH=${IMAGE_PATH}/${HDB_NAME}.tar.bz
HDA=${IMAGE_PATH}/${HDA_NAME}.vdi
HDB=${LOCAL_PATH}/${HDB_NAME}.vdi
SHARED_FOLDER=${LOCAL_PATH}/${SHARED_FOLDER_NAME}
MACHINE_FOLDER=${HOME}/.VirtualBox

function check_vbox()
{
    if [ ! -d $HOME/.VirtualBox ]; then
    echo "    ###############################################################"
    echo "    #                                                             #"
    echo "    #   You don't appear to have a ~/.VirtualBox directory        #"
    echo "    #   Hit <Return> to initialize VirtualBox,                    #"
    echo "    #   (agree to the VirtualBox licence if asked)                #"
    echo "    #   and then close VirtualBox.                                #"
    echo "    #                                                             #"
    echo "    ###############################################################"
    echo ""
    echo -n "    Hit <Return>..."
    read in
    VirtualBox
    echo ""
    fi

    if [ -f $HOME/.VirtualBox/VirtualBox.xml ]; then
        agree=`cat $HOME/.VirtualBox/VirtualBox.xml | grep LicenseAgreed | wc -l`
      if [ `expr $agree ==  1` -eq 0 ]; then
        echo "    ###############################################################"
        echo "    #                                                             #"
        echo "    #   Sorry, but there doesn't appear to be confirmation of     #"
        echo "    #   licence acceptance in your VirtualBox config file...      #"
        echo "    #   ...let's give it another go.                              #"
        echo "    #   Hit <Return> to restart VirtualBox,                       #"
        echo "    #   (agree to the VirtualBox licence if asked)                #"
        echo "    #   and then close VirtualBox.                                #"
        echo "    #   If you don't get prompted to accept the licence,          #"
        echo "    #   just close VirtualBox and we'll carry on anyway.          #"
        echo "    #                                                             #"
        echo "    ###############################################################"
        echo ""
        echo -n "    Hit <Return>..."
          read in
        VirtualBox
        echo ""
      fi
    fi
}

function gen_start_script()
{
    echo "#!/bin/sh
VMNAME=$VM
VirtualBox -startvm \$VMNAME" > startvm.sh;
    chmod a+x startvm.sh
}

function is_ok()
{
    if [ $? -eq 0 ]; then
    echo "              [   OK   ]"
    else
    sd=`dirname $0`
    echo "              [ FAILED ]"
    echo "  Now you might do something and try to run this script again. Trying to to do: "
    echo "  [STEP-1] Remove everything in your vm directory by: 'rm -rf *'"
    echo "  [STEP-2] Delete .VirtualBox by: 'rm -rf \$HOME/.VirtualBox'"
    echo "  [STEP-3] Starting VirtualBox by: 'VirtualBox'"
    echo "  [STEP-4] Make sure you agree with the VirtualBox license (if it asks you to do so). "
    echo "  [STEP-5] If you find $VM in VirtualBox, delete it."
    echo "  [STEP-6] Exit VirtualBox."
    echo "  [STEP-7] Re-execute this script again: $sd/setup.sh"
    exit
    fi
}

function check_env()
{
    if [ ! -f $HDA ]; then
    echo "Could not access file $HDA! Make sure you have specified the correct path."
    exit
    fi

    if [ ! -f $HDB_DOWNLOAD_PATH ]; then
    echo "Could not access file $HDB_DOWNLOAD_PATH! Make sure you have specified the correct path."
    exit
    fi
}

function get_hdb_image()
{
    path=$1

    if [ -f $HDB ]; then
    dt=`date +'%y-%m-%d-%H:%M:%S'`
    mv ${HDB} ${LOCAL_PATH}/${HDB_NAME}.${dt}.bak
    echo ""
    echo -n "   ${HDB_NAME}.vdi has been backuped as ${HDB_NAME}.${dt}.bak"

    tar vjxf $path > /dev/null
    echo "  [   OK   ]"
    else
    tar vjxf $path > /dev/null
    is_ok
    fi
}

function re_init_vm()
{
    vbe=$1
    echo -n "   2-1. Deleting ${VM}"
    if [ "$vbe" -eq 1 ]; then
    ${VMM_TOOL} unregistervm ${VM} -delete > /dev/null
    rm -rf ${VM} > /dev/null 2>&1
    if [ $? -eq 1 ]; then
        echo "                  [ FAILED ]"
        echo "  ERROR: Make sure you have closed VirtualBox and try again. Or you can delete ${VM} manually."
        exit
    else
        echo "                  [   OK   ]"
    fi

    else
    rm -rf ${VM} > /dev/null 2>&1
    echo "                  [   OK   ]"
    fi

    echo -n "   2-2. Re-init the Virtual Machine ..."
    ${VMM_TOOL} createvm -name ${VM} -register -ostype Linux -basefolder ${LOCAL_PATH} > /dev/null

    if [ $? -eq 1 ]; then
    echo "              [ FAILED ]"
    echo "  ERROR: Make sure you have closed VirtualBox and try again. Or you can delete ${VM} manually."
    exit
    else
    echo "              [   OK   ]"
    fi
}

function init_vm()
{
    vbe=0
    vmc=`${VMM_TOOL} list vms | grep ${VM} | wc -l`

    if [ `expr $vmc \>= 1` -eq 1 ]; then
    vbe=1
    else
    if [ -d ${VM} ]; then
        vbe=2
    fi
    fi

    if [ `expr $vbe \> 0` -eq 1 ]; then
    echo "              [ FAILED ]"
    echo -n "   ERROR: '${VM}' already exists! Do you want to delete it?"
    echo -n "[Y/N]:"
    read answer
    if [ "$answer" == "Y" ]; then
        re_init_vm $vbe
    else
        if [ "$answer" == "y" ]; then
        re_init_vm $vbe
        else
        echo "          [EXITED]"
        exit;
        fi
    fi
    else
    ${VMM_TOOL} createvm -name ${VM} -register -ostype Linux -basefolder ${LOCAL_PATH} > /dev/null
    is_ok
    fi
}

function is_hd_existed()
{
    if [ $? -eq 0 ]; then
    echo "              [   OK   ]"
    else
    echo "              [ FAILED ]"
    echo "  ERROR: Hard disks might already exist."
    echo "  This probably is fine."
    echo "  Otherwise, you need to delete '${HDA_NAME}.vdi' and '${HDB_NAME}.vdi' manually from the Virtual Media Manager."
    fi
}

clear
check_env
check_vbox

echo -n "1. Registering the Virtual Machine ... "
init_vm
${VMM_TOOL} setproperty machinefolder $MACHINE_FOLDER

echo -n "2. Setting-up the Virtual Machine ...  "
${VMM_TOOL} storagectl ${VM} --name ${SATA_CONTROLLER} --add sata --controller IntelAHCI > /dev/null
${VMM_TOOL} modifyvm ${VM} --boot1 disk --nic1 nat --nictype1 82540EM --macaddress1 080027f32aff --memory ${MEMORY} # > /dev/null
is_ok

echo -n "3. Downloading the disk image...        "
get_hdb_image $HDB_DOWNLOAD_PATH

echo -n "4. Registering hard disks...       "

${VMM_TOOL} storageattach ${VM} --storagectl ${SATA_CONTROLLER} --port 0 --device 0 --type hdd --medium ${HDA} # > /dev/null
${VMM_TOOL} storageattach ${VM} --storagectl ${SATA_CONTROLLER} --port 1 --device 0 --type hdd --medium ${HDB} # > /dev/null

# detach ${HDA}, set immutable, and re-attach

${VMM_TOOL} storageattach ${VM} --storagectl ${SATA_CONTROLLER} --port 0 --device 0 --type hdd --medium none
${VMM_TOOL} modifyhd --type immutable ${HDA}
${VMM_TOOL} storageattach ${VM} --storagectl ${SATA_CONTROLLER} --port 0 --device 0 --type hdd --medium ${HDA}

# enable PAE, disable audio

${VMM_TOOL} modifyvm ${VM} --pae on
${VMM_TOOL} modifyvm ${VM} --audio none

is_hd_existed

echo -n "5. Setting-up shared folder (${SHARED_FOLDER_NAME})..."
mkdir -p ${SHARED_FOLDER}
${VMM_TOOL} sharedfolder add ${VM} -name ${SHARED_FOLDER_NAME} -hostpath ${SHARED_FOLDER} # > /dev/null
is_ok

gen_start_script

echo " "
echo "    Your virtual machine '${VM}' is now ready."
echo "    You can start the VM with './startvm.sh' or 'VirtualBox'."
echo " "
echo "    As root, use \"mount -t vboxsf dummynetshared /mnt/shared\""
echo "    to mount the folder \"${SHARED_FOLDER}\" on the VM. "
echo "    Your shared folder is '/mnt/shared' on the VM."
echo " "
echo "    ###############################################################"
echo "    #                                                             #"
echo "    #   Put your data in '/work' or in '/mnt/shared' -            #"
echo "    #   otherwise data will be lost when you close the VM.        #"
echo "    #                                                             #"
echo "    ###############################################################"
echo " "

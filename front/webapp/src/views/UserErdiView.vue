<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">

        <v-container class="px-2 py-1 tasks-container">
            <v-layout align-start justify-space-between row class="mb-3">
                <v-flex md3 style="cursor: pointer" @click="backToUserErdiList">
                    <v-layout row class="mt-2">
                        <v-icon color="black" class="mr-2">arrow_back</v-icon>
                        <span class="subheading">Назад</span>
                    </v-layout>
                </v-flex>
                <v-spacer></v-spacer>
<!--                <v-flex md3 class="pa-0" v-if="(eventStatus === 'NEW' || eventStatus === 'FORMED' || eventStatus === 'SCHEDULED') && !planned">
                    <v-btn class="ma-0 right" color="grey" @click="fillEvent" :disabled="loadData" :loading="fillArrLoading"
                           title="Заполнить мероприятия списком проверяемых ресурсов">
                        Заполнить мероприятие
                    </v-btn>
                </v-flex>-->
            </v-layout>

            <v-layout class="mb-3" justify-start row>
                <v-card class="record-card pa-3" style="width: 100%">
                    <v-card-title class="font-weight-bold pa-0 pb-4">
                        <v-flex align-self-center md6 style="font-size: larger">
                        </v-flex>
                        <v-spacer></v-spacer>
                    </v-card-title>
                    <v-card-text class="pa-0">
                        <v-layout v-if="!createMode" row class="pa-0 mb-2 lineText">
                            <v-flex md4 xs4 class="name-font">
                                ID
                            </v-flex>
                            <v-flex md8 xs8 class="value-font">
                                {{ id | noData }}
                            </v-flex>
                        </v-layout>
                        <v-layout row class="pa-0 mb-2 lineText">
                            <v-flex md4 xs4 class="name-font">
                                <span>Название</span>
                            </v-flex>
                            <v-flex md8 xs8 class="value-font">
                                <span v-if="!editMode && !createMode">{{ userErdiData.name | noData }}</span>
                                <v-text-field v-if="editMode || createMode" v-model="userErdiData.name"
                                              class="custom-text-field"
                                ></v-text-field>
                            </v-flex>
                        </v-layout>
                        <v-layout row class="pa-0 mb-2 lineText">
                            <v-flex md4 xs4 class="name-font">
                                Нарушение
                            </v-flex>
                            <v-flex md8 xs8 class="value-font">
                                <span>{{ violationName | noData }}</span>
                                <v-btn v-if="editMode || createMode" color="primary" flat @click="openSubtypeDialog">Выбрать нарушение</v-btn>
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout row class="pa-0 my-2" justify-space-between>
                            <h3 class="ml-2">Список записей</h3>
                            <v-btn v-if="editMode || createMode" color="primary" flat @click="openCreateUnitDialog">Добавить запись</v-btn>
                        </v-layout>
                        <v-layout row class="pa-0 mb-2">
                            <v-flex md12 >
                                <v-data-table
                                        :headers="headers"
                                        :items="customErdiUnits"
                                        class="mx-4"
                                        hide-actions
                                >
                                    <template v-slot:items="props">
                                        <td>{{ props.item.type | noData}}</td>
                                        <td>{{ props.item.value | noData}}</td>
                                        <template v-if="createMode || editMode">
                                            <td class="text-xs-right">
                                                    <v-icon class="icon-btn" color="primary" @click="openEditUnitDialog(props.item)" >edit</v-icon>
                                                    <v-icon class="icon-btn" color="primary" @click="deleteUnit(props.item)">delete</v-icon>
                                            </td>
                                        </template>
                                    </template>
                                    <template v-slot:no-data v-if="!loadData">
                                        <v-alert :value="true" color="warning" icon="warning">
                                            Нет записей для отображения.
                                        </v-alert>
                                    </template>
                                </v-data-table>
                            </v-flex>
                        </v-layout>
<!--                        <v-layout row class="pa-0 mb-2">
                            <v-flex md6 class="cube pa-0" v-if="false">
                                <v-layout align-end row class="mb-3" v-if="editMode">
                                    <v-spacer></v-spacer>
                                    <v-flex md4 class="pa-0">
                                        <v-btn flat class="ma-0 right" @click="editMode = !editMode">Отмена</v-btn>
                                    </v-flex>
                                    <v-flex md4 class="pa-0">
                                        <v-btn class="ma-0 right" color="primary">Сохранить</v-btn>
                                    </v-flex>
                                </v-layout>
                                <v-btn icon class="ma-0 right" color="primary" title="Редактировать" @click="editMode = !editMode"><v-icon>edit</v-icon></v-btn>
                            </v-flex>
                        </v-layout>-->
                    </v-card-text>
                    <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-btn v-if="editMode || createMode" flat @click="cancelEdit">Отменить</v-btn>
                        <v-btn v-if="editMode || createMode" color="primary" @click="save" :disabled="!isValid">Сохранить</v-btn>
                        <v-btn v-if="!editMode && !createMode" class="ma-0 right mr-2" color="primary" title="Редактировать"
                               @click="editMode = !editMode">
                            Редактировать
                        </v-btn>
                    </v-card-actions>
                </v-card>
            </v-layout>
        </v-container>
        <!--<v-dialog v-model="dialogUnitDelete" max-width="290">
            <v-card>
                <v-card-title class="headline">Удаление записи</v-card-title>
                <v-card-text>Вы точно хотите удалить запись #{{chosenUnit? chosenUnit.id : ''}}?</v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn flat @click="closeDeleteUnitDelete">Отмена</v-btn>
                    <v-btn color="error" @click="deleteUnit">Удалить</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>-->
        <dialog-user-erdi-unit v-model="dialogUnitEdit" :unit.sync="chosenUnit" @updateUnit = updateUnit :action=action></dialog-user-erdi-unit>
        <dialog-subtype-table v-model="dialogSubtype" @linkSubtype="linkSubtype"></dialog-subtype-table>
    </v-layout>
</template>

<script>
    import DialogUserErdiUnit from "../components/DialogUserErdiUnit";
    import DialogSubtypeTable from "../components/DialogSubtypeTable";

    export default {
        name: "UserErdiView",

        components: {DialogSubtypeTable, DialogUserErdiUnit},

        props: [],

        data() {
            return {
                title: null,
                loadData: false,

                id: null,
                userErdiData: {},
                violationName: null,
                customErdiUnits: [],
                buffer: {},

                editMode: false,
                createMode: false,

                dialogUnitDelete: false,
                dialogUnitEdit: false,
                dialogSubtype: false,

                chosenUnit: null,
                action: null
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            headers() {
                if (this.editMode || this.createMode) {
                    return [
                        {text: 'Тип записи', value: 'type', sortable: true},
                        {text: 'Значение', value: 'value', sortable: true},
                        {text: 'Действия', value: 'actions', align: 'right', sortable: false}
                    ]
                }
                else {
                    return [
                        {text: 'Тип записи', value: 'type', sortable: true},
                        {text: 'Значение', value: 'value', sortable: true}
                    ]
                }
            },

            isValid() {
                return !(this.userErdiData.name == '' || this.userErdiData.subtypeId == null);
            }

/*            violationName() {
                    if (this.userErdiData.violationId) {
                    //return "йоу йоу " + this.userErdiData.violationId;
                    const config = () => ({params: {origId: this.userErdiData.violationId}});
                    return this.$axios.get(this.$urls.SUBTYPE_ITEM_INFO, config())
                        .then(resp => {
                            return resp.data.violationName
                        })
                        .catch(() => {
                            this.showInfoMsg({message: "Ошибка получения данных ЕРДИ"});
                            return null
                        })
                }
            }*/
        },

        watch: {
            'userErdiData.subtypeId'(v) {
                if (v) {
                    const config = () => ({params: {origId: v}});
                    this.$axios.get(this.$urls.SUBTYPE_ITEM_INFO, config())
                        .then(resp => {
                            this.violationName = resp.data.registryName + (resp.data.violationName !== null ? " (" + resp.data.violationName +")" : '');
                        })
                        .catch(() => {
                            this.showInfoMsg({message: "Ошибка получения данных ЕРДИ"});
                        })
                }
            },

            editMode(v) {
                if (v) {
                    console.log("we edit");
                    this.buffer.userErdiData = Object.assign({}, this.userErdiData);
                    this.buffer.customErdiUnits = this.customErdiUnits.slice(0);
                }
                else {
                    this.buffer = {};
                }
            }
        },

        mounted() {
            this.id = this.$route.params.user_erdi_id;
            if (this.id != null) {
                this.showUserErdi();
            } else {
                this.createUserErdi();
            }
            this.$store.commit('setSection', `${this.title}`);
        },

        methods: {
            showUserErdi() {
                this.title = `Пользовательское ЕРДИ # ${this.id}`;
                this.createMode = false;
                this.getInfo();
            },

            createUserErdi() {
                this.title = `Создание нового пользовательского ЕРДИ`;
                this.createMode = true;
                this.userErdiData = {
                    name: null,
                    subtypeId: null
                };
                this.customErdiUnits = [];
            },

            getInfo() {
/*                this.userErdiData = {
                    "name": "Тест 1",
                    "violationId": 1,
                };

                this.customErdiUnits = [
                    {
                        "id": 22,
                        "type": "IP_V4",
                        "value": "104.31.87.1"
                    },
                    {
                        "id": 23,
                        "type": "URL",
                        "value": "http://tbib.org/images/4607/72ceaf765d2472a4d278168fb16821de6409ff1a.jpeg?5121380"
                    }
                ]*/
                this.$axios.get(this.$urls.CUSTOM_ERDI + '/' + this.id).then(resp => {
                    this.userErdiData = {
                        name: resp.data.name,
                        subtypeId: resp.data.subtypeId
                    };

                    this.customErdiUnits = resp.data.customErdiUnits;
                    //notification
                }).catch(e => {
                    console.log('error', e);
                })
                .finally(() => {
                    this.loadData = false;
                });
            },

            openSubtypeDialog() {
              this.dialogSubtype = true;
            },

            linkSubtype(item) {
                console.log("link subtype " + item.origId);
                this.userErdiData.subtypeId = item.origId;
            },

            openDeleteUnitDialog(unit) {
                this.chosenUnit = unit;
                this.dialogUnitDelete = true;
            },

            closeDeleteUnitDelete() {
                this.chosenUnit = null;
                this.dialogUnitDelete = false;
            },

            deleteUnit(unit) {
/*                if (this.chosenUnit && this.chosenUnit.id) {*/
                    console.log("deleting Unit with id " + unit.id);
                    this.customErdiUnits = this.customErdiUnits.filter(item => {
                        return item.id !== unit.id;
                    });
/*                }*/
                this.chosenUnit = null;
                this.dialogUnitDelete = false;
            },

            openEditUnitDialog(unit) {
                this.chosenUnit = unit;
                this.action = 'edit';
                this.dialogUnitEdit = true;
            },

            openCreateUnitDialog() {
                this.chosenUnit = null;
                this.action = 'create';
                this.dialogUnitEdit = true;
            },

            updateUnit() {
                if (this.action === 'edit')
                    this.customErdiUnits.forEach((item, idx) => {
                        if (item.id === this.chosenUnit.id) {
                            this.$set(this.customErdiUnits, idx, this.chosenUnit);
                        }
                    });
                else if (this.action === 'create') {
                    this.customErdiUnits.push(this.chosenUnit)
                }
                this.chosenUnit = null;
            },

            cancelEdit() {
                console.log("cancel edit");
                if (this.id) {
                    this.userErdiData = Object.assign({}, this.buffer.userErdiData);
                    this.customErdiUnits = this.buffer.customErdiUnits.slice(0);
                    this.editMode = false;
                }
                else {
                    this.backToUserErdiList();
                }
            },

            save() {
              console.log("post save");
              let data = this.userErdiData;
              delete data.id;
              data.customErdiUnits = this.customErdiUnits;
              data.customErdiUnits.forEach(function(v){ delete v.id });
              console.log("jjj" + data.customErdiUnits[1]);

              let url = this.$urls.CUSTOM_ERDI;
              if (this.id) {
                  this.$axios.put(url + '/' + this.id, data).then(resp => {
                      console.log("put done")
                      //notification
                  }).catch(e => {
                      console.log('error', e);
                  })
                  .finally(() => {
                      this.editMode = false;
                      this.backToUserErdiList();
                  });
              }
              else {
                  this.$axios.post(url, data).then(resp => {
                      console.log("post done")
                      //notification
                  }).catch(e => {
                      console.log('error', e);
                  })
                  .finally(() => {
                      this.editMode = false;
                      this.backToUserErdiList();
                  });
              }
            },

            backToUserErdiList() {
                this.$router.push({name: 'directorylocal', params: {dir_type: 'user_erdi'}});
            },
        }
    }
</script>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
    .custom-text-field {
        width: 50%;
        font-size: 14px !important;
        padding-top: 0;
        margin-top: 0;
    }
</style>
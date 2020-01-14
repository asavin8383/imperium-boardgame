<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-card class="record-card pa-3" style="width: 60%">
        <v-card-title class="font-weight-bold pa-0 pb-4">
            <v-flex align-self-center md6 style="font-size: larger">
                Просмотр записи
            </v-flex>
            <v-spacer></v-spacer>
            <v-icon @click="closeCard">close</v-icon>
        </v-card-title>
        <v-card-text class="pa-0">
            <v-layout row class="pa-0 mb-2">
                <v-flex md4 class="name-font">
                    ID
                </v-flex>
                <v-flex md8 class="value-font">
                    {{ recordInternal.id | noData }}
                </v-flex>
            </v-layout>
            <v-layout row class="pa-0 mb-2">
                <v-flex md4 class="name-font">
                    Название
                </v-flex>
                <v-flex md8 class="value-font">
                    {{ recordInternal.name | noData }}
                </v-flex>
            </v-layout>
            <v-layout row class="pa-0 mb-2">
                <v-flex md4 class="name-font">
                    Нарушение
                </v-flex>
                <v-flex md8 class="value-font">
                    {{ recordInternal.violationName | noData }}
                </v-flex>
            </v-layout>
            <v-layout row class="pa-0 mb-2">
                <v-flex md12 >
                <v-data-table
                        :headers="headers"
                        :items="recordInternal.customErdiUnits"
                        class="mx-4"
                        hide-actions
                >
                    <template v-slot:items="props">
                        <td>{{ props.item.id | noData}}</td>
                        <td>{{ props.item.type | noData}}</td>
                        <td>{{ props.item.value | noData}}</td>
                    </template>
                    <template v-slot:no-data v-if="!loadData">
                        <v-alert :value="true" color="warning" icon="warning">
                            Нет записей для отображения.
                        </v-alert>
                    </template>
                </v-data-table>
                </v-flex>
            </v-layout>
            <v-layout row class="pa-0 mb-2">
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
            </v-layout>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn v-if="editMode" color="red darken-1" flat @click="edit=false">Отменить</v-btn>
            <v-btn v-if="editMode" color="primary" @click="save">Сохранить</v-btn>
            <v-btn v-if="!editMode" class="ma-0 right mr-2" color="primary" title="Редактировать"
                   @click="editMode = !editMode">
                Редактировать
            </v-btn>
<!--            <v-flex md4 class="cube pa-0" v-if="!createMode && !editMode">
                <v-btn v-if="!editMode" class="ma-0 right mr-2" color="primary" title="Редактировать"
                       @click="editMode = !editMode">
                    Редактировать
                </v-btn>
            </v-flex>-->
        </v-card-actions>
    </v-card>
</template>

<script>
    export default {
        name: "DirUserErdiCard",

        props: ['value', 'record'],
        /*        props: {
                    value: '',
                    id: null,
                    type: null
                },*/

        data() {
            return {
                loadData: false,
                recordInternal: Object.assign({}, this.record),
                headers: [
                    {text: 'ID', value: 'id', sortable: false},
                    {text: 'Тип записи', value: 'type', sortable: false},
                    {text: 'Значение', value: 'value', sortable: false}
                ],

                editMode: false,
                createMode: false
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            show: {
                set: function (v) {
                    this.$emit('input', v)
                },
                get: function () {
                    return this.value;
                }
            }
        },

        watch: {
            record(v) {
                this.recordInternal = v;
            }
        },

        mounted() {
        },

        methods: {
            closeCard() {
                this.show = false;
            }
        }
    }
</script>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
</style>
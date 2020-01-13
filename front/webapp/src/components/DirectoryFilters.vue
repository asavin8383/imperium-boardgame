<template>
    <v-card flat class="filters-card mx-3">
        <v-card-text>
<!--            <v-btn @click="addFilterRow">Добавить +</v-btn>-->
            <v-layout align-start justify-space-between row class="pa-0">
                <v-flex md8>
                    <v-layout column class="cube">
                        <v-layout row>
                            <v-btn icon class=" ma-1 left" color="primary" title="Добавить" @click="addFilterRow">
                                <v-icon>add</v-icon>
                            </v-btn>
                        </v-layout>
                        <v-layout column>
                            <template v-for="(filter, idx) in filters">
                                <v-layout row justify-start class="mt-1">
                                    <v-flex md5 xs4 class="filter-row-element" style="padding-right: 5%">
                                        <v-select
                                                v-model="filter.field"
                                                class="areaTextSize"
                                                :items="computeFieldsList(filter.field)"
                                                item-text="label"
                                                item-value="field"
                                                hide-details
                                                prepend-icon="mdi-filter"
                                                @change="$emit('newFilters', {filters: filters, validate: haveWrongField})"
                                                placeholder="Выберите поле фильтра">
                                        </v-select>
                                    </v-flex>
                                    <v-flex md5 xs6 class="filter-row-element pr-3 my-2">
                                        <v-text-field
                                                :ref="'datefield-' + filter.field"
                                                v-if="filter.field == null || filter.field === 'id' || filter.field === 'resourceValue' || filter.field === 'startTime' || filter.field === 'endTime'"
                                                v-model="filter.value"
                                                class="areaTextSize"
                                                @input="checkField(filter, $event)"
                                                @blur="checkField(filter, 'blur')"
                                                :placeholder="(filter.field === 'startTime' || filter.field === 'endTime') ? 'Введите время в формате гггг-мм-дд' : 'Введите значение фильтра'"></v-text-field>
                                        <v-autocomplete v-else
                                                        class="areaTextSize"
                                                        placeholder="Введите значение фильтра"
                                                        v-model="filter.value"
                                                        @change="$emit('newFilters', {filters: filters, validate: haveWrongField})"
                                                        :items="valueLists.find(x => x.fieldType === filter.field).values"
                                                        color="primary"
                                                        multiple
                                        ></v-autocomplete>
                                    </v-flex>
                                    <v-flex md2 xs2 class="cube pa-0 pl-2">
                                        <!--                        <v-btn @click="removeFilterRow(idx)">
                                                                    удалить
                                                                </v-btn>-->
                                        <v-btn icon class=" ma-1 left" color="error" title="Удалить" @click="removeFilterRow(idx)">
                                            <v-icon>remove</v-icon>
                                        </v-btn>
                                    </v-flex>
                                </v-layout>
                            </template>
                        </v-layout>
                    </v-layout>
                </v-flex>
                <v-flex md4>
                    <v-layout column class="mt-1">
                        <v-checkbox
                                v-model="randomMode"
                                label="Добавить случайные ЕРДИ"
                        ></v-checkbox>
                    </v-layout>
                </v-flex>
            </v-layout>
        </v-card-text>
        <v-card-actions class="pa-3">
            <v-spacer></v-spacer>
            <v-btn flat @click="clearFilters">Очистить фильтры</v-btn>
            <v-btn class="primary" @click="activateFilters">Применить</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script>
    export default {
        name: "DirectoryFilters",
        props: ['dirData'],

        data() {
            return {
                filters: [],

                allFields: [
                    { field: 'id', label: 'ID' },
                    { field: 'resourceValue', label: 'Ресурс' },
                    { field: 'resourceTypes', label: 'Тип ресурса' },
                    { field: 'decisionOrgs', label: 'Орган, принявший решение' },
                    { field: 'infoTypeIds', label: 'Идентификатор типа нарушения' },
                    { field: 'registryNames', label: 'Реестр' },
                    { field: 'categoryNames', label: 'Категория' },
                    { field: 'violationNames', label: 'Нарушение' },
                    { field: 'startTime', label: 'Время начала' },
                    { field: 'endTime', label: 'Время завершения' },
                ],
                valueLists: [],
                randomMode: false,
                userCountMode: false,
                userCount: null,
                haveWrongField: false,
                mapWrongFields: new Map(),

                //fields: []
            }
        },

        mounted() {
        },

        watch: {
            dirData(v) {
                if (v) {
                    //this.createAllFields(v);
                    let promiseArr = [];
                    let self = this;
                    this.allFields.forEach(item => {
                        if (item.field !== 'id' && item.field !== 'resourceValue' && item.field !== 'startTime' && item.field !== 'endTime') {
                            promiseArr.push(
                                self.$axios.get(this.$urls.POD_ERDI + "/" + item.field)
                                    .then(resp => {
                                        return {
                                            fieldType: item.field,
                                            values: resp.data
                                        }
                                    }));
                        }
                    });
                    Promise.all(promiseArr)
                        .then(resultsArr => {
                            self.valueLists = resultsArr;
                        });
                }
            },

            userCountMode(v) {
                if (!v) this.userCount = null;
            },
        },

        computed: {
/*            allFields() {
                return this.dirData ? Object.keys(this.dirData) : null;
            }*/
        },

        methods: {
/*            createAllFields(data) {
                let temp = Object.keys(data);
                temp.forEach((item, idx, arr) => {
                    if (item !== 'id' && item !== 'resourceValue')
                        arr[idx] = item + 's';
                });
                //лейблы?
                this.allFields = temp;
            },*/

            addFilterRow() {
                this.filters.push({
                    field: null,
                    value: null
                })
            },

            removeFilterRow(idx) {
                console.log("idx: " + idx);

                if(this.mapWrongFields.has(this.filters[idx].field)){
                    this.mapWrongFields.delete(this.filters[idx].field);
                }
                if(!this.mapWrongFields.size)
                    this.haveWrongField = false;

                this.filters.splice(idx, 1);

                this.$emit('newFilters', {filters: this.filters, validate: this.haveWrongField});
            },

            clearFilters() {
                this.filters = [];
                this.randomMode = false;
                this.userCountMode = false;
            },

            activateFilters() {
                if(this.haveWrongField) return;
                let filtersData =  {};
                filtersData.filters = [...this.filters];
                if (this.randomMode)
                    filtersData.filters.push({field: 'random', value: true});
                if (this.userCount)
                    filtersData.userCount = this.userCount;
                //this.$emit('filterSearch', this.filters);
                this.$emit('filterSearch', filtersData);
            },

            computeFieldsList(field) {
                let temp = [...this.allFields];
                this.filters.forEach(filter => {
                    let idx = temp.indexOf(filter.field);
                    if (idx !== -1) temp.splice(idx, 1);
                });
                if (field) temp.push(field);
                return temp
            },

            checkField(filter, value) {
                console.log(filter);

                if(filter && value === "blur"){
                    value = this.$refs['datefield-' + filter.field][0].value;
                }

                console.log(this.$refs['datefield-' + filter.field][0]);

                if(filter.field === "startTime" || filter.field === "endTime"){
                    console.log(value);

                    let regexp = /^((19|20)\d\d)[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])$/;

                    if(value.match(regexp)){
                        console.log(111);
                        this.haveWrongField = false;
                        this.$refs['datefield-' + filter.field][0].$el.style.border = "none";

                        this.mapWrongFields.delete(filter.field);
                    }else{
                        console.log(222);
                        this.haveWrongField = true;
                        this.$refs['datefield-' + filter.field][0].$el.style.border = "1px solid red";

                        this.mapWrongFields.set(filter.field, 1);
                    }
                }
                this.$emit('newFilters', {filters: this.filters, validate: this.haveWrongField});
            }
        }
    }
</script>

<style>
    .v-card.filters-card.v-sheet {
        /*border: 1px solid #1976d2;*/
        border: 1px solid rgba(0,0,0,0.54);
    }

    .filter-row-element {
/*        padding-right: 5%;*/
    }

    .number-text-field.v-text-field {
        /*width: 40px;*/
    }


</style>